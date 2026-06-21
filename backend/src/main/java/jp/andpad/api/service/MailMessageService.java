package jp.andpad.api.service;

import java.util.List;

import org.springframework.stereotype.Service;

import jp.andpad.api.domain.SentMailMessage;
import jp.andpad.api.repository.MailMessageRepository;
import jp.andpad.api.security.TenantContext;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MailMessageService {

    private final MailMessageRepository mailMessageRepository;

    public List<SentMailMessage> listSentMessages(String entityType, String entityId, Integer limit) {
        int resolvedLimit = limit != null ? limit : 50;
        return mailMessageRepository.list(TenantContext.orgId(), entityType, entityId, resolvedLimit);
    }
}
