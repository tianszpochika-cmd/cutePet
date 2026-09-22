package com.cutepet.iam;

import com.cutepet.iam.Repos.TransferRepo;
import org.springframework.stereotype.Component;

/** TransferRepoStub 的 Spring Data 实现（接口在 Services.java 尾部定义）。 */
@Component
class TransferRepoAdapter implements TransferRepoStub {

    private final TransferRepo repo;

    TransferRepoAdapter(TransferRepo repo) {
        this.repo = repo;
    }

    @Override
    public OwnershipTransferEntity save(OwnershipTransferEntity entity) {
        if (entity.createdAt == null) {
            entity.createdAt = java.time.LocalDateTime.now();
        }
        return repo.save(entity);
    }

    @Override
    public OwnershipTransferEntity find(long id) {
        return repo.findById(id)
                .orElseThrow(() -> new com.cutepet.common.web.NotFoundException("转移单不存在"));
    }
}
