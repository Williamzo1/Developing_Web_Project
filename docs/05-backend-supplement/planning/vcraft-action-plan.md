# vCraft — Kế hoạch Thực thi
# Việc cụ thể cho từng người · Bắt đầu ngay hôm nay

> Mốc gốc trước triển khai: V1–V4 đã chạy · Không có V5 · Không có Entity mới · Frontend 1 file main.jsx
> Tất cả thiết kế chi tiết đã có trong 4 tài liệu trước. Tài liệu này chỉ nói: **ai làm gì, theo thứ tự nào, copy từ đâu.**

---

## QUY TẮC CHUNG

1. **Thắng làm M1 Backend trước** — Huy và Dương không bắt đầu M2 cho đến khi V5 đã chạy xanh và `mvn package` thành công.
2. **Mỗi người làm 1 branch riêng** — không push thẳng vào main.
3. **Mỗi PR phải chạy được** — không merge PR làm server crash khi khởi động.
4. **Thứ tự trong mỗi phần là bắt buộc** — số nhỏ hơn phải xong trước.

---

## THẮNG — M1 Backend (làm đầu tiên, ~2–3 ngày)

> Cập nhật 2026-11-24: đã thực hiện đến hết bước 10 trong ZIP này.
> Chi tiết thay đổi, kiểm thử và phần còn cần kiểm trên máy nhóm:
> `../../03-engineering/m1-steps-1-10.md`. Bước 11 chưa được đánh dấu hoàn tất cho môi trường thật.

### Bước 1 · pom.xml — Thêm 4 dependency

Mở `backend/pom.xml`, thêm vào `<dependencies>`:

```xml
<dependency>
    <groupId>net.coobird</groupId>
    <artifactId>thumbnailator</artifactId>
    <version>0.4.20</version>
</dependency>
<dependency>
    <groupId>com.google.zxing</groupId>
    <artifactId>core</artifactId>
    <version>3.5.3</version>
</dependency>
<dependency>
    <groupId>com.google.zxing</groupId>
    <artifactId>javase</artifactId>
    <version>3.5.3</version>
</dependency>
<dependency>
    <groupId>org.apache.tika</groupId>
    <artifactId>tika-core</artifactId>
    <version>2.9.1</version>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-mail</artifactId>
</dependency>
```

Chạy `mvn compile` — phải xanh trước khi làm bước tiếp theo.

---

### Bước 2 · application.properties + application-local.properties

Thêm vào `backend/src/main/resources/application.properties`:
```properties
spring.servlet.multipart.max-file-size=5MB
spring.servlet.multipart.max-request-size=55MB
spring.servlet.multipart.enabled=true
```

Thêm vào `backend/src/main/resources/application-local.properties`:
```properties
app.base-url=http://localhost:8080
app.frontend-url=http://localhost:5173
media.storage-path=/tmp/vcraft-media
media.max-file-size-mb=5
media.max-files-per-request=10
cors.allowed-origins=http://localhost:5173
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=${MAIL_USERNAME:}
spring.mail.password=${MAIL_PASSWORD:}
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true
auth.session-hours=168
auth.otp-minutes=15
```

---

### Bước 3 · V5 Migration — Tạo file mới

Tạo file: `backend/src/main/resources/db/migration/V5__expand_core.sql`

**Copy toàn bộ nội dung từ:** `vcraft-backend-blueprint.md` → Phần B → "V5__expand_core.sql"

> File này bao gồm: ALTER workshops, ALTER accounts, CREATE email_otps, CREATE media, CREATE media_links, CREATE certificates, CREATE workshop_hours, CREATE workshop_closures, ALTER products, CREATE process_steps, CREATE batches, CREATE batch_milestones, và 12 tag seed.

Tạo file: `backend/src/main/resources/db/migration/V6__news.sql`

**Copy nội dung từ:** `vcraft-backend-blueprint.md` → Phần B → "V6__news.sql"

---

### Bước 4 · Cập nhật Entity — Bắt buộc trước khi khởi động server

`ddl-auto=validate` sẽ crash nếu Entity không khớp schema. Phải cập nhật Entity **cùng lúc** với migration.

#### Workshop.java — Viết lại hoàn toàn

Thay toàn bộ nội dung file hiện tại:

```java
package vn.vcraft.backend.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "workshops")
public class Workshop {
    @Id
    private Long id;
    private String slug;
    private String name;
    private String description;
    private String familyHistory;
    private String address;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private String contactEmail;
    private String phone;
    private String facebookUrl;
    private Boolean approved;
    private Instant approvedAt;
    private Instant lockedAt;
    private String lockedReason;
    private Boolean featured;
    private Long coverMediaId;
    private Boolean acceptsVisitors;
    private Instant createdAt;
    private Instant updatedAt;

    protected Workshop() {}

    // Getters
    public Long getId()            { return id; }
    public String getSlug()        { return slug; }
    public String name()           { return name; }   // giữ method name() cho code cũ dùng
    public String getName()        { return name; }
    public String getDescription() { return description; }
    public String getFamilyHistory(){ return familyHistory; }
    public String getAddress()     { return address; }
    public BigDecimal getLatitude(){ return latitude; }
    public BigDecimal getLongitude(){ return longitude; }
    public String getContactEmail(){ return contactEmail; }
    public String getPhone()       { return phone; }
    public String getFacebookUrl() { return facebookUrl; }
    public Boolean getApproved()   { return approved; }
    public Instant getApprovedAt() { return approvedAt; }
    public Instant getLockedAt()   { return lockedAt; }
    public String getLockedReason(){ return lockedReason; }
    public Boolean getFeatured()   { return featured; }
    public Long getCoverMediaId()  { return coverMediaId; }
    public Boolean getAcceptsVisitors(){ return acceptsVisitors; }
    public Instant getCreatedAt()  { return createdAt; }
    public Instant getUpdatedAt()  { return updatedAt; }

    // Setters (dùng trong Service)
    public void setName(String v)           { name = v; }
    public void setDescription(String v)    { description = v; }
    public void setFamilyHistory(String v)  { familyHistory = v; }
    public void setAddress(String v)        { address = v; }
    public void setLatitude(BigDecimal v)   { latitude = v; }
    public void setLongitude(BigDecimal v)  { longitude = v; }
    public void setContactEmail(String v)   { contactEmail = v; }
    public void setPhone(String v)          { phone = v; }
    public void setFacebookUrl(String v)    { facebookUrl = v; }
    public void setApproved(Boolean v)      { approved = v; }
    public void setApprovedAt(Instant v)    { approvedAt = v; }
    public void setLockedAt(Instant v)      { lockedAt = v; }
    public void setLockedReason(String v)   { lockedReason = v; }
    public void setFeatured(Boolean v)      { featured = v; }
    public void setCoverMediaId(Long v)     { coverMediaId = v; }
    public void setAcceptsVisitors(Boolean v){ acceptsVisitors = v; }
    public void setUpdatedAt(Instant v)     { updatedAt = v; }
}
```

#### Product.java — Thêm field mới (giữ code cũ)

Thêm vào sau `private String imagePath;`:

```java
private java.math.BigDecimal referencePrice;
private Boolean priceVisible;
private Boolean hidden;
private Boolean featured;
private java.time.Instant createdAt;
private java.time.Instant updatedAt;
```

Thêm getters (sau `getCategory()`):
```java
public java.math.BigDecimal getReferencePrice() { return referencePrice; }
public Boolean getPriceVisible() { return priceVisible; }
public Boolean getHidden()       { return hidden; }
public Boolean getFeatured()     { return featured; }
public java.time.Instant getCreatedAt() { return createdAt; }
public java.time.Instant getUpdatedAt() { return updatedAt; }

// Setters
public void setName(String v)                      { name = v; }
public void setDescription(String v)               { description = v; }
public void setImagePath(String v)                 { imagePath = v; }
public void setReferencePrice(java.math.BigDecimal v){ referencePrice = v; }
public void setPriceVisible(Boolean v)             { priceVisible = v; }
public void setHidden(Boolean v)                   { hidden = v; }
public void setFeatured(Boolean v)                 { featured = v; }
public void setCreatedAt(java.time.Instant v)      { createdAt = v; }
public void setUpdatedAt(java.time.Instant v)      { updatedAt = v; }
```

---

### Bước 5 · Tạo các Entity mới

Tạo 4 file trong `vn/vcraft/backend/model/`:

**ProcessStep.java** — Copy từ `vcraft-backend-phase-c.md` → Phần 4.2

**Batch.java** — Copy từ `vcraft-services-and-tests.md` → Phần 1.3 (xem Batch entity fields)

Nội dung Batch.java:
```java
package vn.vcraft.backend.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.*;

@Entity
@Table(name = "batches")
public class Batch {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long productId;
    private Long workshopId;
    private String code;
    private String publicCode;
    private String name;
    private String description;
    private String kind;
    private Integer quantity;
    private String unit;
    private BigDecimal referencePrice;
    private Boolean priceVisible;
    private String supplyCapacity;
    private LocalDate availableFrom;
    private String visibility;
    private String shareToken;
    private String partnerNote;
    private Boolean hidden;
    private Boolean featured;
    private Instant createdAt;
    private Instant updatedAt;

    protected Batch() {}

    // Getters
    public Long getId()               { return id; }
    public Long getProductId()        { return productId; }
    public Long getWorkshopId()       { return workshopId; }
    public String getCode()           { return code; }
    public String getPublicCode()     { return publicCode; }
    public String getName()           { return name; }
    public String getDescription()    { return description; }
    public String getKind()           { return kind; }
    public Integer getQuantity()      { return quantity; }
    public String getUnit()           { return unit; }
    public BigDecimal getReferencePrice(){ return referencePrice; }
    public Boolean getPriceVisible()  { return priceVisible; }
    public String getSupplyCapacity() { return supplyCapacity; }
    public LocalDate getAvailableFrom(){ return availableFrom; }
    public String getVisibility()     { return visibility; }
    public String getShareToken()     { return shareToken; }
    public String getPartnerNote()    { return partnerNote; }
    public Boolean getHidden()        { return hidden; }
    public Boolean getFeatured()      { return featured; }
    public Instant getCreatedAt()     { return createdAt; }
    public Instant getUpdatedAt()     { return updatedAt; }

    // Setters
    public void setProductId(Long v)        { productId = v; }
    public void setWorkshopId(Long v)       { workshopId = v; }
    public void setCode(String v)           { code = v; }
    public void setPublicCode(String v)     { publicCode = v; }
    public void setName(String v)           { name = v; }
    public void setDescription(String v)    { description = v; }
    public void setKind(String v)           { kind = v; }
    public void setQuantity(Integer v)      { quantity = v; }
    public void setUnit(String v)           { unit = v; }
    public void setReferencePrice(BigDecimal v){ referencePrice = v; }
    public void setPriceVisible(Boolean v)  { priceVisible = v; }
    public void setSupplyCapacity(String v) { supplyCapacity = v; }
    public void setAvailableFrom(LocalDate v){ availableFrom = v; }
    public void setVisibility(String v)     { visibility = v; }
    public void setShareToken(String v)     { shareToken = v; }
    public void setPartnerNote(String v)    { partnerNote = v; }
    public void setHidden(Boolean v)        { hidden = v; }
    public void setFeatured(Boolean v)      { featured = v; }
    public void setCreatedAt(Instant v)     { createdAt = v; }
    public void setUpdatedAt(Instant v)     { updatedAt = v; }
}
```

**BatchMilestone.java:**
```java
package vn.vcraft.backend.model;

import jakarta.persistence.*;
import java.time.*;

@Entity
@Table(name = "batch_milestones")
public class BatchMilestone {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long batchId;
    private Long processStepId;
    private String title;
    private String note;
    private LocalDate happenedOn;
    private Instant createdAt;

    protected BatchMilestone() {}

    public Long getId()              { return id; }
    public Long getBatchId()         { return batchId; }
    public Long getProcessStepId()   { return processStepId; }
    public String getTitle()         { return title; }
    public String getNote()          { return note; }
    public LocalDate getHappenedOn() { return happenedOn; }
    public Instant getCreatedAt()    { return createdAt; }

    public void setBatchId(Long v)        { batchId = v; }
    public void setProcessStepId(Long v)  { processStepId = v; }
    public void setTitle(String v)        { title = v; }
    public void setNote(String v)         { note = v; }
    public void setHappenedOn(LocalDate v){ happenedOn = v; }
    public void setCreatedAt(Instant v)   { createdAt = v; }
}
```

**NewsPost.java:**
```java
package vn.vcraft.backend.model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "news_posts")
public class NewsPost {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long authorAccountId;
    private String slug;
    private String title;
    private String body;
    private String kind;
    private Instant eventStart;
    private Instant eventEnd;
    private String eventPlace;
    private Boolean hidden;
    private Instant publishedAt;
    private Instant createdAt;
    private Instant updatedAt;

    protected NewsPost() {}

    public Long getId()              { return id; }
    public Long getAuthorAccountId() { return authorAccountId; }
    public String getSlug()          { return slug; }
    public String getTitle()         { return title; }
    public String getBody()          { return body; }
    public String getKind()          { return kind; }
    public Instant getEventStart()   { return eventStart; }
    public Instant getEventEnd()     { return eventEnd; }
    public String getEventPlace()    { return eventPlace; }
    public Boolean getHidden()       { return hidden; }
    public Instant getPublishedAt()  { return publishedAt; }
    public Instant getCreatedAt()    { return createdAt; }
    public Instant getUpdatedAt()    { return updatedAt; }

    public void setAuthorAccountId(Long v) { authorAccountId = v; }
    public void setSlug(String v)          { slug = v; }
    public void setTitle(String v)         { title = v; }
    public void setBody(String v)          { body = v; }
    public void setKind(String v)          { kind = v; }
    public void setEventStart(Instant v)   { eventStart = v; }
    public void setEventEnd(Instant v)     { eventEnd = v; }
    public void setEventPlace(String v)    { eventPlace = v; }
    public void setHidden(Boolean v)       { hidden = v; }
    public void setPublishedAt(Instant v)  { publishedAt = v; }
    public void setCreatedAt(Instant v)    { createdAt = v; }
    public void setUpdatedAt(Instant v)    { updatedAt = v; }
}
```

---

### Bước 6 · Tạo các file mới trong storage/ và validation/

**Tạo package `vn.vcraft.backend.storage`** (thư mục mới):

- `MediaStorage.java` — Copy từ `vcraft-backend-phase-c.md`
- `LocalFileStorage.java` — Copy từ cùng tài liệu

**Thêm vào `vn.vcraft.backend.validation`:**
- `SlugUtils.java` — Copy từ `vcraft-backend-phase-c.md` → Phần 0.7

---

### Bước 7 · Tạo các Repository mới

Trong `vn.vcraft.backend.repository/`:

- `OtpRepository.java` — Copy từ `vcraft-backend-phase-c.md` → Phần 1.4
- `MediaRepository.java` — Copy từ `vcraft-backend-phase-c.md` → Phần 2.3
- `WorkshopHourRepository.java` — Copy từ `vcraft-implementation-guide.md` → Phần 1.2
- `BatchRepository.java` — Copy từ `vcraft-implementation-guide.md` → Phần 1.3
- `BatchMilestoneRepository.java` — Copy từ `vcraft-implementation-guide.md` → Phần 1.4
- `NewsRepository.java` — Copy từ `vcraft-implementation-guide.md` → Phần 1.5
- `ProcessStepRepository.java`:

```java
package vn.vcraft.backend.repository;

import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import vn.vcraft.backend.model.ProcessStep;
import java.util.List;

public interface ProcessStepRepository extends JpaRepository<ProcessStep, Long> {

    @Query("SELECT s FROM ProcessStep s WHERE s.productId = :pid ORDER BY s.position")
    List<ProcessStep> findByProductId(@Param("pid") long productId);

    @Modifying
    @Query("DELETE FROM ProcessStep s WHERE s.productId = :pid")
    void deleteByProductId(@Param("pid") long productId);
}
```

**Mở rộng `AccountRepository.java`** — Thêm 7 method từ `vcraft-implementation-guide.md` → Phần 1.6, bao gồm **cập nhật `findActiveSession`** (thêm kiểm tra `locked_at IS NULL`).

**Mở rộng `WorkshopRepository.java`** — Copy từ `vcraft-implementation-guide.md` → Phần 1.1 (thay toàn bộ nội dung, giữ các method cũ: `list()`, `createPending()`, `approve()`, `rename()`, `updateContact()`).

**Mở rộng `ProductRepository.java`** — Thêm các query từ `vcraft-implementation-guide.md` → Phần 1.7.

---

### Bước 8 · Tạo các Service mới + Mở rộng Service cũ

- `OtpService.java` — Copy từ `vcraft-backend-phase-c.md` → Phần 1.3
- `MediaService.java` — Copy từ `vcraft-backend-phase-c.md` → Phần 2.2
- **Thay thế** `WorkshopService.java` — Copy từ `vcraft-services-and-tests.md` → Phần 1.1
- **Thay thế** `ProductService.java` — Copy từ `vcraft-services-and-tests.md` → Phần 1.2
- `BatchService.java` — Copy từ `vcraft-backend-phase-c.md` → Phần 5.3, **bổ sung** `toDetail()` từ `vcraft-services-and-tests.md` → Phần 1.3
- `AdminService.java` — Copy từ `vcraft-services-and-tests.md` → Phần 1.4
- `HomeService.java` — Copy từ `vcraft-services-and-tests.md` → Phần 1.5
- **Mở rộng** `AuthService.java` — Thêm `register()`, `verifyEmail()`, `resendOtp()` từ `vcraft-backend-phase-c.md` → Phần 1.6

---

### Bước 9 · Tạo Controller mới + Mở rộng Controller cũ

- `CorsConfig.java` (trong `config/`) — Copy từ `vcraft-backend-phase-c.md` → Phần 0.5
- **Cập nhật** `SecurityConfig.java` — Thêm permitAll cho các path mới (xem blueprint Phần 4.2)
- `MediaController.java` — Copy từ `vcraft-backend-phase-c.md` → Phần 2.4
- `WorkshopController.java` (public) — Copy từ `vcraft-backend-phase-c.md` → Phần 3.4
- **Mở rộng** `ArtisanWorkshopController.java` — Thêm 8 endpoint từ `vcraft-backend-phase-c.md` → Phần 3.5
- `ArtisanProductController.java` — Copy từ `vcraft-backend-phase-c.md` → Phần 4.6
- **Mở rộng** `ProductController.java` — Thêm `/{idOrSlug}`, `/{slug}/steps`, query params filter
- `BatchController.java` — Copy từ `vcraft-backend-phase-c.md` → Phần 5.4
- `ArtisanBatchController.java` — Copy từ cùng phần trên
- `NewsController.java` — Copy từ `vcraft-backend-phase-c.md` → Phần 6.2
- `AdminController.java` — Copy từ `vcraft-backend-phase-c.md` → Phần 7.2
- **Mở rộng** `AuthController.java` — Thêm 3 endpoint register từ `vcraft-backend-phase-c.md` → Phần 1.7

---

### Bước 10 · Tạo tất cả DTO mới

Tạo các record/class trong `vn.vcraft.backend.dto/` — Copy từ `vcraft-implementation-guide.md` → Phần 4:

```
WorkshopProfileInput, WorkshopHourInput, WorkshopHourResponse,
WorkshopClosureInput, WorkshopDetailResponse, WorkshopSummaryResponse,
MapPinResponse, CertificateInput, CertificateResponse,
ProductCreateInput, ProductUpdateInput, ProductDetailResponse,
ProductSummaryResponse, StepInput, StepResponse,
BatchCreateInput, BatchUpdateInput, BatchDetailResponse,
BatchSummaryResponse, MilestoneInput, MilestoneResponse,
MediaResponse, RegisterRequest, RegisterResponse,
VerifyEmailRequest, ResendOtpRequest,
NewsCreateInput, NewsResponse, AdminStatsResponse,
FeaturedInput, LockInput, TagInput, TagResponse, HomeResponse
```

---

### Bước 11 · Kiểm tra M1 Backend

```bash
# 1. Chạy migration
mvn spring-boot:run -Dspring-boot.run.profiles=local
# Phải thấy: Flyway migrated to version 6 (news)

# 2. Build
mvn package -DskipTests

# 3. Test thủ công nhanh
curl -s http://localhost:8080/api/health | python3 -m json.tool
curl -s http://localhost:8080/api/workshops | python3 -m json.tool
curl -s -X POST http://localhost:8080/api/register \
  -H "Content-Type: application/json" \
  -d '{"username":"testbuyer","password":"Test@12345","displayName":"Test","email":"test@test.com","phone":"0901234567"}' \
  | python3 -m json.tool
# Phải trả 201 (OTP gửi nếu có mail config, hoặc log lỗi mail nhưng account vẫn tạo)
```

**Khi bước 11 xanh → Báo Huy và Dương bắt đầu M2.**

---

## HUY — M2 Luồng A: Triển lãm (~1 tuần, bắt đầu sau Bước 11 của Thắng)

### Thứ tự làm

```
1. npm install → chạy frontend dev server
2. Tạo cấu trúc thư mục frontend (xem vcraft-implementation-guide.md Phần 3.1)
3. Viết src/api/client.js (Phần 3.2)
4. Viết src/auth/AuthContext.jsx + RequireAuth.jsx (Phần 3.3, 3.4)
5. Viết src/components/Layout.jsx (Header + Footer + Outlet)
6. Viết src/main.jsx module hóa + routing (Phần 3.5)
   → Giữ /san-pham và /san-pham/:id cũ hoạt động (backward compat)
7. Viết src/pages/auth/Login.jsx
8. Test: đăng nhập → token lưu sessionStorage → /api/me → hiển thị tên
9. Viết src/pages/WorkshopList.jsx (gọi GET /api/workshops)
10. Viết src/pages/WorkshopDetail.jsx (gọi GET /api/workshops/{slug})
11. Viết src/pages/Map.jsx (Leaflet — xem Phần 3.7)
    → npm install leaflet react-leaflet (thêm vào package.json Phần 3.8)
12. Viết src/pages/ProductList.jsx (thêm filter workshopId, categoryId, tagSlug)
13. Viết src/pages/ProductDetail.jsx (gọi GET /api/products/{idOrSlug})
    → Hiển thị công đoạn sản xuất từ /api/products/{slug}/steps
14. Viết src/pages/artisan/WorkshopProfile.jsx
    → Form sửa hồ sơ: tên, mô tả, lịch sử, địa chỉ, tọa độ, Facebook
    → Phần giờ mở cửa (theo thứ 0–6)
    → Danh sách và thêm/xóa chứng nhận
15. Viết src/pages/artisan/ProductManage.jsx
    → Danh sách sản phẩm của hộ (kể cả ẩn)
    → Form tạo/sửa sản phẩm
    → Form công đoạn (PUT /api/artisan/products/{id}/steps)
    → Nút ẩn sản phẩm
```

### Checkpoint — Huy báo cả nhóm khi xong

- [ ] Đăng nhập/đăng xuất hoạt động, token lưu đúng
- [ ] /ho-gia-dinh hiển thị danh sách xưởng
- [ ] /ho-gia-dinh/{slug} hiển thị hồ sơ đầy đủ
- [ ] /ban-do hiển thị ghim Leaflet, bấm ghim mở popup
- [ ] /san-pham lọc được theo hộ và phân loại
- [ ] Artisan vào /ho-so-xuong, sửa hồ sơ, reload thấy thay đổi
- [ ] Artisan tạo sản phẩm mới → hiện ở /san-pham

---

## DƯƠNG — M2 Luồng B: Truy xuất + Bản đồ (~1 tuần, song song với Huy)

### Thứ tự làm

```
1. Đọc BatchService.java từ tài liệu → hiểu cơ chế public_code và share_token
2. Viết src/pages/BatchDetail.jsx (landing page từ QR — Phần 3.6 của implementation-guide)
   → Route: /batches/:publicCode
   → Đọc ?token= từ URL nếu có
   → Hiển thị: thông tin lô, sản phẩm, xưởng, timeline mốc, ảnh
3. Viết src/pages/artisan/BatchManage.jsx
   → Danh sách lô (GET /api/artisan/batches)
   → Form tạo lô (chọn sản phẩm, loại, số lượng, visibility)
   → Form thêm mốc (tiêu đề, ngày, ghi chú, liên kết công đoạn)
   → Nút sinh share token + hiển thị URL + nút thu hồi
   → Hiển thị QR (<img src="/api/artisan/batches/{id}/qr.png">)
   → Nút tải QR (download link)
4. Thêm route /batches/:publicCode vào main.jsx (phối hợp với Huy)
5. Test quét QR bằng điện thoại thật (mạng di động, không phải localhost)
   → Dùng ngrok hoặc IP LAN: VITE_API_BASE=http://192.168.x.x:8080
6. Viết compose.yaml bổ sung volume media_data + nginx.conf /media/
   → Copy từ vcraft-backend-blueprint.md Phần H
7. Chuẩn bị VPS deployment:
   → docker compose up -d
   → Kiểm tra QR trỏ đúng domain thật
```

### Checkpoint — Dương báo cả nhóm khi xong

- [ ] /batches/{publicCode} hiển thị landing page
- [ ] Lô PRIVATE: /batches/{code} → 404 (không thấy), /batches/{code}?token=X → thấy
- [ ] Artisan tạo lô → sinh public_code tự động
- [ ] Artisan sinh share token → URL có token
- [ ] Thu hồi share token → URL cũ → 404
- [ ] GET /batches/{code}/qr.png → ảnh PNG quét được
- [ ] Quét QR điện thoại thật → mở đúng landing page
- [ ] VPS chạy được, domain thật trỏ đúng

---

## THẮNG — M2 Luồng C: Frontend Báo giá (~1 tuần, sau M1 xong)

Backend Quote đã hoàn chỉnh. Chỉ cần làm Frontend + 1 sửa nhỏ backend.

### Bước backend nhỏ — Làm trước

Mở `QuoteRequestService.java`, trong method `create()`, thêm kiểm tra email đã xác thực:

```java
// Thêm vào đầu method create(), trước các validation khác:
if (!accounts.isEmailVerified(actor.id())) {
    throw new ApiException(
        org.springframework.http.HttpStatus.FORBIDDEN,
        "EMAIL_NOT_VERIFIED",
        "Vui lòng xác thực email trước khi tạo yêu cầu báo giá.");
}
```

Thêm `isEmailVerified()` vào `AccountRepository` (xem `vcraft-implementation-guide.md` → Phần 1.6).

### Thứ tự làm Frontend

```
1. src/pages/auth/Register.jsx
   → Form: username, password, displayName, email, phone
   → Gọi POST /api/register → redirect /xac-thuc-email
2. src/pages/auth/VerifyEmail.jsx
   → Input 6 chữ số OTP
   → Gọi POST /api/register/verify-email
   → Nút gửi lại OTP (POST /api/register/resend-otp)
3. src/pages/buyer/QuoteRequestCreate.jsx
   → Form: tiêu đề, số lượng, đơn vị, phân loại, chế độ (mở/chỉ định), tags
   → Nút Lưu nháp + Gửi
4. src/pages/buyer/QuoteRequestList.jsx
   → Danh sách yêu cầu của Buyer (GET /api/buyer/quote-requests)
   → Hiển thị trạng thái bằng màu/nhãn
5. src/pages/buyer/QuoteRequestDetail.jsx
   → Chi tiết yêu cầu + danh sách báo giá nhận được
   → Nút chấp nhận báo giá (POST .../accept với version)
   → Nút hủy yêu cầu
   → Timeline sự kiện
6. src/pages/artisan/QuoteInbox.jsx
   → Danh sách yêu cầu phù hợp (GET /api/artisan/quote-requests)
   → Badge số yêu cầu mới (đếm từ response)
7. src/pages/artisan/QuoteDetail.jsx
   → Chi tiết yêu cầu + form gửi báo giá
   → Nút sửa báo giá (PUT) + rút báo giá (POST withdraw)
   → Nút Đang sản xuất (start-production) + Đã giao (deliver)
```

### Checkpoint — Thắng báo cả nhóm khi xong

- [ ] Buyer đăng ký → nhận OTP → xác thực → đăng nhập
- [ ] Buyer tạo yêu cầu → gửi → Artisan phù hợp thấy trong inbox
- [ ] Artisan gửi báo giá → Buyer thấy ngay
- [ ] Buyer chấp nhận → trạng thái ACCEPTED
- [ ] Thử chấp nhận 2 tab song song → 1 thành công, 1 nhận 409
- [ ] Artisan cập nhật IN_PRODUCTION → DELIVERED

---

## M3 — Quản trị & Hoàn thiện (Cả nhóm · sau M2)

Phân theo thời gian rảnh, không song song chặt:

| Việc | Người | Copy từ |
|---|---|---|
| src/pages/admin/WorkshopAdmin.jsx | Huy | — |
| src/pages/admin/NewsAdmin.jsx | Thắng | — |
| src/pages/admin/Dashboard.jsx (stats) | Dương | — |
| Trang /tin-tuc + /tin-tuc/{slug} | Huy | — |
| Trang /api/home → trang chủ động | Huy | — |
| Admin nổi bật (PUT /api/admin/featured) | Thắng | — |
| Admin hậu kiểm (POST .../hide) | Thắng | — |

---

## M4 — Test & Demo (~3 ngày trước bảo vệ)

### Script test tự động

```bash
# Chạy m1.py + quotes.py đã có (giữ nguyên, không sửa)
python backend/tests/m1.py
python backend/tests/quotes.py

# Chạy m2.py mới (copy từ vcraft-services-and-tests.md Phần 2)
python backend/tests/m2.py
```

### Checklist bảo mật — Test thủ công trước khi demo

```
[ ] Artisan A truy cập PUT /api/artisan/products/{id của Artisan B} → 403
[ ] Buyer A truy cập GET /api/buyer/quote-requests/{id của Buyer B} → 404
[ ] GET /api/batches/{private_code} không có token → 404
[ ] Upload file .pdf → 415
[ ] Upload ảnh 10MB → 413
[ ] Hộ bị khóa → gọi API ngay → 401
[ ] Buyer chưa verify email → POST /buyer/quote-requests → 403 EMAIL_NOT_VERIFIED
```

### Seed dữ liệu demo (trên VPS, 2 ngày trước demo)

```
- 5 hộ có tọa độ thật (Bát Tràng, Gia Lâm)
- Mỗi hộ: 3–5 sản phẩm có ảnh thật
- Mỗi sản phẩm: 3–6 công đoạn sản xuất
- 2 lô có mốc sản xuất + ảnh
- 1 lô PRIVATE với share token
- 3 tin tức đã publish
- 1 yêu cầu báo giá đang có 2 báo giá (để demo bước 10–12)
```

### Demo script (13 bước — 15 phút)

Xem chi tiết trong `docs/04-plan/demo-script.md` của repo. Thứ tự từ Blueprint:

```
1. Khách → trang chủ → danh sách hộ → hồ sơ hộ (ảnh, lịch sử, chứng nhận)
2. Khách → danh mục → lọc theo hộ + phân loại → chi tiết sản phẩm (công đoạn)
3. Khách → bản đồ → ghim → popup → hồ sơ hộ
4. Admin → cấp hộ mới → duyệt → hộ đăng nhập được
5. Artisan → sửa hồ sơ + upload ảnh → tạo sản phẩm + công đoạn
6. Artisan → tạo lô + mốc → hệ thống sinh QR
7. Điện thoại → quét QR → landing page
8. Artisan → chuyển lô PRIVATE → liên kết riêng
9. Buyer → đăng ký + xác thực email → tạo yêu cầu báo giá
10. 2 Artisan → thấy yêu cầu → gửi báo giá
11. Buyer → chấp nhận 1 báo giá → thử tab thứ 2 → 409
12. Artisan → cập nhật IN_PRODUCTION → DELIVERED
13. Admin → đăng tin tức → xem thống kê → chọn nổi bật
```

---

## TÀI LIỆU THAM CHIẾU

| Cần làm gì | Đọc tài liệu nào | Phần nào |
|---|---|---|
| SQL migration V5 đầy đủ | `vcraft-backend-blueprint.md` | Phần B |
| Code mẫu Controller/DTO/Service | `vcraft-backend-phase-c.md` | Phần 1–7 |
| Repository SQL queries | `vcraft-implementation-guide.md` | Phần 1 |
| Seed data bổ sung | `vcraft-implementation-guide.md` | Phần 2 |
| Frontend cấu trúc + AuthContext + Map | `vcraft-implementation-guide.md` | Phần 3 |
| WorkshopService/ProductService/BatchService toDetail() | `vcraft-services-and-tests.md` | Phần 1 |
| Test script m2.py | `vcraft-services-and-tests.md` | Phần 2 |
| Security matrix, endpoint list đầy đủ | `vcraft-backend-blueprint.md` | Phần D, E |
| Câu hỏi vấn đáp | `docs/04-plan/demo-script.md` trong repo | — |
