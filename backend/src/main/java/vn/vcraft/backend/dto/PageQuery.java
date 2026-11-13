package vn.vcraft.backend.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public class PageQuery {
    @Min(value = 0, message = "Trang bắt đầu từ 0.")
    @Max(value = 100000, message = "Trang không vượt quá 100000.")
    private int page = 0;

    @Min(value = 1, message = "Kích thước trang ít nhất 1.")
    @Max(value = 50, message = "Kích thước trang tối đa 50.")
    private int size = 12;

    public int getPage() { return page; }
    public void setPage(int page) { this.page = page; }
    public int getSize() { return size; }
    public void setSize(int size) { this.size = size; }
}
