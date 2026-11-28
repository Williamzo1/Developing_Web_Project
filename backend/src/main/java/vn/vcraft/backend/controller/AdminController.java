package vn.vcraft.backend.controller;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springframework.validation.annotation.Validated;
import org.springframework.http.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import vn.vcraft.backend.dto.*;
import vn.vcraft.backend.model.Actor;
import vn.vcraft.backend.service.*;
import java.util.*;

@RestController
@Validated
@RequestMapping("/api/admin")
public class AdminController {
    private final AdminService admin;
    private final WorkshopService workshops;
    public AdminController(AdminService admin, WorkshopService workshops) {
        this.admin = admin;
        this.workshops = workshops;
    }

    @PostMapping("/workshops/{id}/lock") @ResponseStatus(HttpStatus.NO_CONTENT)
    void lock(@PathVariable long id,@Valid @RequestBody LockInput i) { workshops.lock(id,i.reason()); }
    @PostMapping("/workshops/{id}/unlock") @ResponseStatus(HttpStatus.NO_CONTENT)
    void unlock(@PathVariable long id) { workshops.unlock(id); }
    @GetMapping("/stats") AdminStatsResponse stats() { return admin.getStats(); }
    @GetMapping("/tags") List<TagResponse> tags() { return admin.listTags(); }
    @PostMapping("/tags") @ResponseStatus(HttpStatus.CREATED)
    TagResponse createTag(@Valid @RequestBody TagInput i) { return admin.createTag(i); }
    @PutMapping("/tags/{id}")
    TagResponse updateTag(@PathVariable long id,@Valid @RequestBody TagInput i) { return admin.updateTag(id,i); }
    @DeleteMapping("/tags/{id}") @ResponseStatus(HttpStatus.NO_CONTENT)
    void deleteTag(@PathVariable long id) { admin.deleteTag(id); }
    @PostMapping("/products/{id}/hide") @ResponseStatus(HttpStatus.NO_CONTENT)
    void hideProduct(@PathVariable long id) { admin.hideProduct(id); }
    @PostMapping("/batches/{id}/hide") @ResponseStatus(HttpStatus.NO_CONTENT)
    void hideBatch(@PathVariable long id) { admin.hideBatch(id); }
    @PutMapping("/featured") @ResponseStatus(HttpStatus.NO_CONTENT)
    void featured(@Valid @RequestBody FeaturedInput i) { admin.setFeatured(i); }
    @GetMapping("/news")
    PageResponse<NewsResponse> news(@Valid @ModelAttribute PageQuery q) { return admin.listNewsAdmin(q); }
    @PostMapping("/news") @ResponseStatus(HttpStatus.CREATED)
    NewsResponse createNews(@AuthenticationPrincipal Actor a,@Valid @RequestBody NewsCreateInput i) { return admin.createNews(a,i); }
    @PutMapping("/news/{id}")
    NewsResponse updateNews(@AuthenticationPrincipal Actor actor,@PathVariable long id,@Valid @RequestBody NewsCreateInput i) { return admin.updateNews(actor,id,i); }
    @DeleteMapping("/news/{id}") @ResponseStatus(HttpStatus.NO_CONTENT)
    void deleteNews(@PathVariable long id) { admin.deleteNews(id); }

}
