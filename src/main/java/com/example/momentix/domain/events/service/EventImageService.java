package com.example.momentix.domain.events.service;


import com.example.momentix.domain.common.exception.event.EventErrorCode;
import com.example.momentix.domain.common.exception.event.EventErrorException;
import com.example.momentix.domain.events.entity.Events;
import com.example.momentix.domain.events.entity.eventimages.EventImages;
import com.example.momentix.domain.events.repository.EventsRepository;
import com.example.momentix.domain.events.repository.eventimages.EventImagesRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service
@RequiredArgsConstructor
public class EventImageService {
    private final S3UploadService s3UploadService;
    private final EventsRepository eventsRepository;
    private final EventImagesRepository eventImagesRepository;

    @Transactional
    public String uploadImage(Long eventId, String imageType, MultipartFile file) throws IOException {
        String imageUrl = s3UploadService.upload(file, imageType);

        Events event = eventsRepository.findById(eventId)
                .orElseThrow(() -> new EventErrorException(EventErrorCode.EVENT_NOT_FOUND));

        EventImages eventImages = eventImagesRepository.findByEvents(event)
                .orElse(new EventImages(event));

        if ("poster".equalsIgnoreCase(imageType)) {
            eventImages.updatePosterImageUrl(imageUrl);
        } else if ("detail".equalsIgnoreCase(imageType)) {
            eventImages.updateDetailImageUrl(imageUrl);
        } else {
            throw new EventErrorException(EventErrorCode.EVENT_NOT_FOUND);
        }
        eventImagesRepository.save(eventImages);

        return imageUrl;
    }
}
