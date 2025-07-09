package com.example.TacoHub.Controller.NotionCopyController;

import com.example.TacoHub.Dto.NotionCopyDTO.BlockDTO;

import com.example.TacoHub.Service.NotionCopyService.BlockService;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;

/**
 * 블록 관리 REST API Controller
 * 블록 CRUD 및 계층 구조 관리 기능 제공
 */
@RestController
@RequestMapping("/api/blocks")
@RequiredArgsConstructor
@Slf4j
public class BlockController {


}
