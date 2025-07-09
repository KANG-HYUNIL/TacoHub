package com.example.TacoHub.Controller.NotionCopyController;

import com.example.TacoHub.Converter.NotionCopyConveter.PageConverter;
import com.example.TacoHub.Dto.NotionCopyDTO.BlockDTO;
import com.example.TacoHub.Dto.NotionCopyDTO.PageDTO;

import com.example.TacoHub.Dto.NotionCopyDTO.Response.PageWithBlocksResponse;
import com.example.TacoHub.Entity.NotionCopyEntity.PageEntity;
import com.example.TacoHub.Service.NotionCopyService.BlockService;
import com.example.TacoHub.Service.NotionCopyService.PageService;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;

/**
 * 페이지 관리 REST API Controller
 * 페이지 CRUD 및 블록 연동 기능 제공
 */
@RestController
@RequestMapping("/api/pages")
@RequiredArgsConstructor
@Slf4j
public class PageController {




 
}
