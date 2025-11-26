package com.aksps.BillWise.service;

import com.aksps.BillWise.model.ProductAuditLog;
import com.aksps.BillWise.repository.ProductAuditLogRepository;
import com.aksps.BillWise.security.jwt.JwtTokenProvider;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;

@Service
public class AuditService {

    private final ProductAuditLogRepository repo;
    private final JwtTokenProvider tokenProvider;
    private final HttpServletRequest request;

    public AuditService(ProductAuditLogRepository repo,
                        JwtTokenProvider tokenProvider,
                        HttpServletRequest request) {
        this.repo = repo;
        this.tokenProvider = tokenProvider;
        this.request = request;
    }

    public void logChange(Long productId, String field, Object oldV, Object newV) {
        String token = tokenProvider.resolveToken(request);
        String username = tokenProvider.getUsernameFromToken(token);

        ProductAuditLog log = new ProductAuditLog(
                null,
                productId,
                field,
                String.valueOf(oldV),
                String.valueOf(newV),
                username,
                null
        );

        repo.save(log);
    }
}
