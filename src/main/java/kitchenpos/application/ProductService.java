package kitchenpos.application;

import java.util.List;
import kitchenpos.dao.ProductDao;
import kitchenpos.domain.Product;
import kitchenpos.dto.ProductDto;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class ProductService {
    private final ProductDao productDao;

    public ProductService(final ProductDao productDao) {
        this.productDao = productDao;
    }

    @Transactional
    public Product create(final ProductDto productDto) {
        return productDao.save(productDto.toEntity());
    }

    public List<Product> list() {
        return productDao.findAll();
    }
}
