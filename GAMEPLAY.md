# GAMEPLAY.md

# Crossword Duel — Gameplay Design

## 1. Mục tiêu
Người chơi điền các từ vào bảng ô chữ theo đúng yêu cầu từ các chữ cái có sẵn. Tất cả các từ phải nằm trong từ điển mà Server cung cấp. Người chơi nào điền được nhiều ô đúng hơn trong thời gian giới hạn sẽ chiến thắng.

---

## 2. Luật chơi
- Server khởi tạo **bảng ô chữ** với các ô:
  - Ô trống: người chơi có thể điền.
  - Ô khoá: chứa sẵn ký tự, không thể thay đổi.
  - Ô chặn: không sử dụng.
- Người chơi nhập ký tự vào ô trống. Client gửi lên Server để kiểm tra hợp lệ:
  - Nếu ký tự hợp lệ (nằm trong từ điển và khớp với các giao cắt) → ô được xác nhận.
  - Nếu không hợp lệ → từ chối, ô vẫn trống.
- Mỗi ô đúng được xác nhận sẽ cộng điểm cho người chơi.
- Nếu hoàn tất một từ, người chơi nhận thêm điểm thưởng.
- Nếu 2 người cùng điền 1 ô, Server xác định người nào hợp lệ trước sẽ thắng ô đó.

---

## 3. Cách tính điểm
- +1 điểm cho mỗi ô ký tự đúng.
- +bonus bằng độ dài từ nếu hoàn tất cả một từ.
- Điểm hiển thị realtime cho cả mình và đối thủ:
  - **My Correct Cells: X**
  - **Opponent Correct Cells: Y**

---

## 4. Thời gian
- Thời lượng trận mặc định: **180 giây**.
- Có đồng hồ đếm ngược hiển thị cho cả hai người.
- Trò chơi kết thúc khi:
  - Hết thời gian, hoặc
  - Tất cả ô có thể điền đã được lấp đầy.

---

## 5. Điều kiện thắng
- Người có điểm số cao hơn khi hết thời gian thắng.
- Nếu bằng điểm:
  1. Người hoàn tất nhiều từ hơn thắng.
  2. Nếu vẫn hoà → Server chọn ngẫu nhiên.

---

## 6. Giao diện & Hiển thị
- Bảng ô chữ hiển thị:
  - Trạng thái các ô: trống / pending / đúng / sai.
  - Highlight ô hoặc từ đang chọn.
- Luôn hiển thị:
  - Điểm số của mình.
  - Điểm số của đối thủ.
  - Đồng hồ đếm ngược.
- Phản hồi lỗi rõ ràng nếu nhập sai.

---

## 7. Luồng chơi
1. Người chơi vào phòng → Server gửi bảng và từ điển.
2. Cả hai bắt đầu điền chữ khi đồng hồ chạy.
3. Client gửi ký tự → Server xác nhận → Cập nhật cho cả hai.
4. Điểm số cập nhật realtime.
5. Khi hết giờ hoặc bảng hoàn tất → Server gửi kết quả cuối cùng.

---

## 8. Edge Cases
- Nếu mất kết nối ngắn hạn, người chơi có thể reconnect và nhận lại trạng thái bảng.
- Nếu hai người cùng nhập 1 ô: chỉ người gửi hợp lệ trước được tính.
- Nếu hết giờ trong lúc ô đang chờ xác nhận → ô đó không tính điểm.

---

## 9. Tuỳ chọn mở rộng
- Chế độ chơi Solo luyện tập.
- Power-up: gợi ý 1 ô, highlight slot khả dĩ.
- Bảng xếp hạng toàn cầu.

---
