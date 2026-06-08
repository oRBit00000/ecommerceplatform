package hr.algebra.ecommerceplatform.controller.mvc;

import hr.algebra.ecommerceplatform.dto.ProductDTO;
import hr.algebra.ecommerceplatform.form.ProductSearchForm;
import hr.algebra.ecommerceplatform.service.CategoryService;
import hr.algebra.ecommerceplatform.service.ProductPriceValidationService;
import hr.algebra.ecommerceplatform.service.ProductService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/mvc/products")
public class ProductMvcController {

    private static final String NEW_PRODUCT_VIEW = "newProduct";
    private static final String PRODUCT_DTO_LIST_ATTRIBUTE = "productDTOList";
    private static final String CATEGORIES_ATTRIBUTE = "categories";
    private static final String PRODUCT_DTO_ATTRIBUTE = "productDTO";
    private static final String PRODUCT_SEARCH_FORM_ATTRIBUTE = "productSearchForm";
    private static final String PRODUCT_CATEGORIES_LIST_ATTRIBUTE = "productCategoriesList";

    private final ProductService productService;
    private final CategoryService categoryService;
    private final ProductPriceValidationService productPriceValidationService;

    public ProductMvcController(ProductService productService,
                                CategoryService categoryService,
                                ProductPriceValidationService productPriceValidationService) {
        this.productService = productService;
        this.categoryService = categoryService;
        this.productPriceValidationService = productPriceValidationService;
    }

    @GetMapping("/new")
    public String getNewProductScreen(Model model) {
        model.addAttribute(PRODUCT_DTO_ATTRIBUTE, new ProductDTO());
        model.addAttribute(CATEGORIES_ATTRIBUTE, categoryService.findAll());
        return NEW_PRODUCT_VIEW;
    }

    @GetMapping("/edit/{id}")
    public String getEditProductScreen(@PathVariable Long id, Model model) {
        model.addAttribute(PRODUCT_DTO_ATTRIBUTE, productService.findById(id).orElseThrow());
        model.addAttribute(CATEGORIES_ATTRIBUTE, categoryService.findAll());
        return NEW_PRODUCT_VIEW;
    }

    @PostMapping("/new")
    public String newProductScreenSubmit(@Valid @ModelAttribute ProductDTO productDTO,
                                         BindingResult result,
                                         Model model) {
        productPriceValidationService.validateProductPrice(productDTO)
                .ifPresent(errorMessage -> result.addError(new ObjectError("globalPriceError", errorMessage)));

        if (result.hasErrors()) {
            model.addAttribute(CATEGORIES_ATTRIBUTE, categoryService.findAll());
            return NEW_PRODUCT_VIEW;
        }

        ProductDTO saved = productDTO.getId() == null
                ? productService.save(productDTO)
                : productService.update(productDTO.getId(), productDTO);
        model.addAttribute(PRODUCT_DTO_ATTRIBUTE, saved);
        return "result";
    }

    @GetMapping("/search")
    public String getProductSearchScreen(@RequestParam(required = false) String category, Model model) {
        ProductSearchForm productSearchForm = new ProductSearchForm();
        productSearchForm.setCategory(category);
        model.addAttribute(PRODUCT_SEARCH_FORM_ATTRIBUTE, productSearchForm);
        model.addAttribute(PRODUCT_DTO_LIST_ATTRIBUTE, category == null || category.isBlank()
                ? productService.findAll()
                : productService.findBySearchCriteria(productSearchForm));
        model.addAttribute(PRODUCT_CATEGORIES_LIST_ATTRIBUTE, productService.findProductCategories());
        return "searchProducts";
    }

    @PostMapping("/search")
    public String searchProductsScreenSubmit(@ModelAttribute ProductSearchForm productSearchForm, Model model) {
        model.addAttribute(PRODUCT_SEARCH_FORM_ATTRIBUTE, productSearchForm);
        model.addAttribute(PRODUCT_CATEGORIES_LIST_ATTRIBUTE, productService.findProductCategories());
        model.addAttribute(PRODUCT_DTO_LIST_ATTRIBUTE, productService.findBySearchCriteria(productSearchForm));
        return "searchProducts";
    }

    @PostMapping("/delete")
    public String deleteProduct(@RequestParam Long id) {
        productService.deleteById(id);
        return "redirect:/mvc/admin/dashboard";
    }
}
