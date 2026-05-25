package com.ecommerce.user.interfaces.rest;

import com.ecommerce.user.application.command.LoginCommand;
import com.ecommerce.user.application.service.AuthApplicationService;
import com.ecommerce.user.interfaces.dto.*;
import com.ecommerce.user.interfaces.mapper.UserDtoMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 會員認證 REST Controller
 *
 * <h3>MapStruct 整合</h3>
 * <p>原有手動建立 {@code RegisterCommand.builder()...build()} 改為：
 * {@link UserDtoMapper#toRegisterCommand(RegisterRequest)}，單行替換多行 builder 呼叫。</p>
 */
@Tag(name = "會員認證", description = "會員登入、註冊、身份確認 API（步驟式流程）")
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class AuthController {

    private final AuthApplicationService authService;
    private final UserDtoMapper          userDtoMapper;

    @Operation(
        summary = "步驟1：確認是否為會員",
        description = "前端根據 `exists` 決定下一步：true → 登入；false → 註冊",
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            content = @Content(mediaType = "application/json",
                examples = {
                    @ExampleObject(name = "手機號查詢", value = """
                            {"identifier": "0912345678"}"""),
                    @ExampleObject(name = "Email 查詢", value = """
                            {"identifier": "user@example.com"}""")
                })
        )
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "查詢成功",
            content = @Content(schema = @Schema(example = """
                    {"code":"0000","message":"success","data":{"exists":true}}"""))),
        @ApiResponse(responseCode = "400", description = "格式錯誤")
    })
    @PostMapping("/check")
    public ResponseEntity<Result<Map<String, Boolean>>> check(
            @Valid @RequestBody CheckRequest req) {
        boolean exists = authService.checkExists(req.getIdentifier());
        return ResponseEntity.ok(Result.ok(Map.of("exists", exists)));
    }

    @Operation(
        summary = "步驟2a：新會員註冊",
        description = "新會員填寫完整資料後完成註冊，成功後自動登入回傳 JWT Token",
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            content = @Content(mediaType = "application/json",
                examples = @ExampleObject(value = """
                        {
                          "fullName": "王小明",
                          "phone": "0912345678",
                          "email": "wang@example.com",
                          "birthday": "1990-05-15",
                          "address": "台北市信義區信義路100號",
                          "password": "Password123!"
                        }"""))
        )
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "註冊成功",
            content = @Content(schema = @Schema(example = """
                    {"code":"0000","message":"success","data":{"token":"eyJhbGci...","memberNo":"M83729471"}}"""))),
        @ApiResponse(responseCode = "400", description = "手機號或 Email 已存在")
    })
    @PostMapping("/register")
    public ResponseEntity<Result<AuthResponse>> register(
            @Valid @RequestBody RegisterRequest req) {
        // MapStruct：RegisterRequest → RegisterCommand（取代手動 builder）
        return ResponseEntity.ok(Result.ok(
                authService.register(userDtoMapper.toRegisterCommand(req))));
    }

    @Operation(
        summary = "步驟2b：會員登入",
        description = "已是會員者輸入密碼取得 JWT Token，支援手機號或 Email",
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            content = @Content(mediaType = "application/json",
                examples = {
                    @ExampleObject(name = "用手機號登入", value = """
                            {"identifier": "0912345678", "password": "Password123!"}"""),
                    @ExampleObject(name = "用 Email 登入", value = """
                            {"identifier": "user@example.com", "password": "Password123!"}""")
                })
        )
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "登入成功"),
        @ApiResponse(responseCode = "400", description = "帳號或密碼錯誤")
    })
    @PostMapping("/login")
    public ResponseEntity<Result<AuthResponse>> login(
            @Valid @RequestBody LoginRequest req) {
        return ResponseEntity.ok(Result.ok(
                authService.login(new LoginCommand(req.getIdentifier(), req.getPassword()))));
    }

    @Operation(summary = "健康檢查", description = "確認 user-service 是否正常運行")
    @SecurityRequirement(name = "")
    @GetMapping("/health")
    public ResponseEntity<Result<String>> health() {
        return ResponseEntity.ok(Result.ok("user-service is running"));
    }
}
