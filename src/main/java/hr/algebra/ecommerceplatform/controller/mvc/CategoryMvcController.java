package hr.algebra.ecommerceplatform.controller.mvc;

import hr.algebra.ecommerceplatform.dto.CategoryDTO;
import hr.algebra.ecommerceplatform.service.CategoryService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/mvc/categories")
public class CategoryMvcController {

    private static final String CATEGORY_FORM_VIEW = "categoryForm";

    private final CategoryService categoryService;

    public CategoryMvcController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping("/manage/new")
    public String newCategory(Model model) {
        model.addAttribute("categoryDTO", new CategoryDTO());
        return CATEGORY_FORM_VIEW;
    }

    @GetMapping("/manage/edit/{id}")
    public String editCategory(@PathVariable Long id, Model model) {
        model.addAttribute("categoryDTO", categoryService.findById(id).orElseThrow());
        return CATEGORY_FORM_VIEW;
    }

    @PostMapping("/manage/new")
    public String saveCategory(@Valid @ModelAttribute CategoryDTO categoryDTO, BindingResult result) {
        if (result.hasErrors()) {
            return CATEGORY_FORM_VIEW;
        }
        if (categoryDTO.getId() == null) {
            categoryService.save(categoryDTO);
        } else {
            categoryService.update(categoryDTO.getId(), categoryDTO);
        }
        return "redirect:/mvc/admin/dashboard";
    }

    @PostMapping("/manage/delete")
    public String deleteCategory(@RequestParam Long id, RedirectAttributes redirectAttributes) {
        try {
            categoryService.deleteById(id);
            redirectAttributes.addFlashAttribute("successMessage", "Category deleted successfully.");
        } catch (IllegalStateException ex) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "This category still has products assigned to it. Delete or move those products first.");
        }
        return "redirect:/mvc/admin/dashboard";
    }
}
