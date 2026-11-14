package vn.vcraft.backend.dto;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class CatalogQuery extends PageQuery {
    @Size(max = 160, message = "Từ khóa tối đa 160 ký tự.")
    private String q;
    @Size(max = 120, message = "Mã xưởng tối đa 120 ký tự.")
    private String workshop;
    @Size(max = 120, message = "Mã phân loại tối đa 120 ký tự.")
    private String category;
    @Pattern(regexp = "id|name-asc|name-desc", message = "Kiểu sắp xếp không hợp lệ.")
    private String sort = "id";

    public String getQ() { return q; }
    public void setQ(String q) { this.q = q; }
    public String getWorkshop() { return workshop; }
    public void setWorkshop(String workshop) { this.workshop = workshop; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public String getSort() { return sort; }
    public void setSort(String sort) { this.sort = sort; }
}
