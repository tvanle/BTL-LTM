# Hướng Dẫn Cài Đặt Ngrok Cho Game WordBrain2

## 1. Cài Đặt Ngrok

### Windows:
1. Tải ngrok từ: https://ngrok.com/download
2. Giải nén vào thư mục (ví dụ: D:\AppData\)
3. Đăng ký tài khoản tại: https://dashboard.ngrok.com/signup

### 2. Cấu Hình Authtoken

```bash
# Lấy authtoken từ: https://dashboard.ngrok.com/get-started/your-authtoken
ngrok config add-authtoken YOUR_AUTH_TOKEN_HERE
```

## 3. Chạy Ngrok

```bash
# Mở command prompt mới và chạy:
ngrok http 8080

# Hoặc nếu ngrok ở thư mục khác:
D:\AppData\ngrok.exe http 8080
```

## 4. Kết Quả

Sau khi chạy, bạn sẽ nhận được URL dạng:
```
Forwarding: https://abc123xyz.ngrok-free.app → localhost:8080
```

## 5. Chia Sẻ Link

Gửi link ngrok cho bạn bè:
- **Link để chơi**: https://abc123xyz.ngrok-free.app
- **Mã phòng**: Tạo trong game và chia sẻ

## Lưu Ý

- **Free tier**: URL thay đổi mỗi lần restart
- **Session timeout**: 2 giờ cho free account
- **Bandwidth limit**: 40 connections/minute

## Cách Chơi Qua Ngrok

1. **Host** (người tạo phòng):
   - Chạy game: `mvn spring-boot:run`
   - Chạy ngrok: `ngrok http 8080`
   - Vào game qua: http://localhost:8080
   - Tạo phòng và lấy mã

2. **Players** (người chơi khác):
   - Vào link ngrok (https://xxx.ngrok-free.app)
   - Nhập mã phòng để join
   - Ready và chờ host start game

## Troubleshooting

### Lỗi "Tunnel not found"
- Kiểm tra authtoken đã config chưa
- Thử restart ngrok

### Lỗi "Connection refused"
- Đảm bảo game đang chạy (port 8080)
- Kiểm tra firewall Windows

### Lỗi WebSocket
- Ngrok tự động hỗ trợ WebSocket
- Không cần config thêm