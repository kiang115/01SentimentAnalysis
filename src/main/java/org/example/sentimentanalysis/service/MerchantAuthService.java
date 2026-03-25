package org.example.sentimentanalysis.service;

import org.example.sentimentanalysis.model.Merchants;
import org.example.sentimentanalysis.model.Products;

public interface MerchantAuthService {

    Long getCurrentMerchantIdOrThrow();

    Merchants getCurrentMerchantOrThrow();

    Products getOwnedProductOrThrow(Long productId);

    Products getTagAnalyzableProductOrThrow(Long productId);
}
