package com.community.dogcat.mapper;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.community.dogcat.domain.ImgBoard;
import com.community.dogcat.dto.uploadImage.UploadPostImageResultDTO;

@Component
public class UploadResultMappingImgBoard {

	private final ModelMapper modelMapper;

	@Autowired
	public UploadResultMappingImgBoard(ModelMapper modelMapper) {
		this.modelMapper = modelMapper;
	}

	public UploadPostImageResultDTO convertToDTO(ImgBoard imgBoard) {
		return modelMapper.map(imgBoard, UploadPostImageResultDTO.class);
	}
}
