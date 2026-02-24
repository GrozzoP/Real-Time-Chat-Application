package com.grosso.chat.service;

import com.grosso.chat.mapper.MessageMapper;
import com.grosso.chat.repository.ChatRepository;
import com.grosso.chat.repository.MessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MessageService {

    private final MessageRepository messageRepository;
    private final ChatRepository chatRepository;
    private final MessageMapper mapper;
    private final NotificationService notificationService;
    private final FileService fileService;
}
