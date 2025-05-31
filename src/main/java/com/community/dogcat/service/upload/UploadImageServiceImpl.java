package com.community.dogcat.service.upload;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.IntStream;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.community.dogcat.domain.Post;
import com.community.dogcat.dto.uploadImage.FileInfoDTO;
import com.community.dogcat.dto.uploadImage.ThumbnailRequestPayload;
import com.community.dogcat.repository.upload.UploadRepository;
import com.community.dogcat.service.util.FileProcessingService;
import com.community.dogcat.service.util.async.ThumbnailService;
import com.community.dogcat.util.cache.UploadedImageCaching;
import com.community.dogcat.util.uploader.DeleteTempFiles;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class UploadImageServiceImpl implements UploadImageService {

	// private final AmazonS3 amazonS3;

	@Value("${baseUrl}") private String baseUrl;

	@Value("${tempUploadPath}") private String tempUploadPath;

	@Value("${finalUploadPath}") private String finalUploadPath;

	// 최종 업로드 링크
	@Value("${newUrl}") private String finalUrl;

	// private final S3Uploader s3Uploader;

	// s3업로드 or 게시글 등록 취소 or 백스페이스 summernote 임시 업로드 이미지 파일 삭제
	// 개선, 놔두고 매일 새벽에 한꺼번에 정리 -> I/O 부담 제거
	private final DeleteTempFiles deleteTempFiles;

	private final FileProcessingService fileProcessingService;

	// 이미지 업로드 시 db 저장, 트랜잭션 축소를 위해 moveAndSaveImage에서 분리
	private final UploadMetaService uploadMetaService;


	@Override
	// 개선, 게시글 등록 시 임시 파일을 업로드 성공 폴더로 옮김
	public void moveAndSaveImages(List<String> uuids, List<String> extensions, List<String> originalFileNames,
		Post postNo) {

		String datePath = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
		String baseDir = finalUploadPath + datePath + "/";

		List<FileInfoDTO> infos = IntStream.range(0, uuids.size()).mapToObj(i ->
			FileInfoDTO.builder()
				.uuid(uuids.get(i))
				.extension(extensions.get(i))
				.deletePossible(false)
				.originalName(originalFileNames.get(i))
				.uploadTime(Instant.now())
				.build()).toList();

		uploadMetaService.saveImageToDB(infos, postNo);

		// 비동기 파일 이동 + 캐싱, 썸네일 생성 위임, 파일 이동 후 썸네일 생성 되고 완료 요청을 받음
		fileProcessingService.handleFinalSave(infos, baseDir).join();

		// 비동기 썸네일 생성 (cpuExecutor)
		// 개선, 썸네일은 kafka가 담당
		// fileProcessingService.handleThumbnails(infos, baseDir);
		// kafkaTemplate.send("thumbnail-Generator", new ThumbnailRequestPayload(infos, baseDir));

	}

	// summernote 취소버튼 누를 때 임시파일 제거
	@Override
	public void deleteSummernoteImage(List<String> uuids, List<String> extensions) {

		for (int i = 0; i < uuids.size(); i++) {
			String fileName = uuids.get(i) + extensions.get(i);
			deleteTempFiles.deleteFile(fileName);
		}
	}

	@Override
	public void deleteSummernoteImageWithBackspace(List<String> deletedImageUrl) {

		for (String fileUrl : deletedImageUrl) {
			String fileName = fileUrl.substring(fileUrl.lastIndexOf("/") + 1);

			deleteTempFiles.deleteFile(fileName);
		}
	}

	// jsonArray
	// 썸머노트 업로드후 본문에 insert 하기위해 정보 반환
	@Override
	public String uploadSummerNoteImage(List<MultipartFile> multipartFiles, HttpServletRequest request) {

		JsonObject jsonObject = new JsonObject();
		JsonArray jsonArray = new JsonArray();

		// 이미지 저장 경로 설정
		List<FileInfoDTO> dtos = new ArrayList<>(multipartFiles.size());

		for (MultipartFile multipartFile : multipartFiles) {
			String originalFileName = multipartFile.getOriginalFilename();
			// 확장자 추출
			String extension = originalFileName.substring(originalFileName.lastIndexOf("."));
			// UUID 파일명
			String uuid = UUID.randomUUID().toString();
			String saveFileName = uuid + extension;

			FileInfoDTO dto = FileInfoDTO.builder()
				.uuid(uuid)
				.extension(extension)
				.uploadTime(Instant.now())
				.deletePossible(false)
				.build();

			dtos.add(dto);

			// json 응답용
			JsonObject fileJson = new JsonObject();

			fileJson.addProperty("imageUrl", baseUrl + "/temp/" + saveFileName);
			fileJson.addProperty("uuid", uuid);
			fileJson.addProperty("extension", extension);
			fileJson.addProperty("name", originalFileName);
			// int[] size = calcImageLength(multipartFile);
			// fileJson.addProperty("width", size[0]);
			// fileJson.addProperty("height", size[1]);
			jsonArray.add(fileJson);
		}
		jsonObject.add("files", jsonArray);

		// 임시 파일 비동기 저장, ioExecutor 풀
		fileProcessingService.handleTempSave(dtos, multipartFiles).join();

		return jsonObject.toString();
	}

	// 개선, 비동기 1차 시도, 제대로 이뤄지지 않았음
	// public void moveAndSaveImages(List<String> uuids, List<String> extensions, List<String> originalFileNames,
	// 	Post postNo) throws IOException {
	//
	// 	String baseDir = finalUploadPath + datePath + "/";
	//
	// 	// 락-free 큐
	// 	Queue<ImgBoard> imgQueue = new ConcurrentLinkedQueue<>();
	// 	Queue<FileInfoDTO> infoQueue = new ConcurrentLinkedQueue<>();
	//
	// 	List<CompletableFuture<Void>> tasks = IntStream.range(0, uuids.size())
	// 		.mapToObj(i -> CompletableFuture.runAsync(() -> {
	// 			try {
	// 				semaphore.acquire();
	//
	// 				processImage(uuids.get(i), extensions.get(i), originalFileNames.get(i),
	// 					postNo, baseDir, datePath, imgQueue, infoQueue);
	//
	// 			} catch (InterruptedException | IOException e) {
	// 				throw new RuntimeException(e);
	// 			} finally {
	// 				semaphore.release();
	// 			}
	// 		}, ioExecutor))
	// 		.toList();
	//
	// 	CompletableFuture.allOf(tasks.toArray(new CompletableFuture[0])).join();
	// 	uploadRepository.saveAll(new ArrayList<>(imgQueue));
	// 	uploadedImageCaching.cacheMetadataAddOrDelete(
	// 		new ArrayList<>(infoQueue), Collections.emptyList());
	// }

	// private void processImage(String uuid, String extension, String originalFilename, Post postNo,
	// 	String baseDir, String datePath, Queue<ImgBoard> imgQueue, Queue<FileInfoDTO> infoQueue) throws IOException {
	//
	// 	String prefix = uuid.substring(0, 2);
	// 	String savedName = uuid + extension;
	// 	String tempFile = tempUploadPath + savedName;
	// 	Path tempPath = Path.of(tempFile);
	// 	String destDir = baseDir + prefix;
	// 	String destFile = destDir + "/" + savedName;
	// 	String thumbDir = baseDir + "/" + prefix + "/thumbnail/";
	// 	// 논 블로킹 복사
	//
	// 	Files.createDirectories(Path.of(thumbDir));
	//
	// 	try (AsynchronousFileChannel in = AsynchronousFileChannel.open(
	// 		tempPath, StandardOpenOption.READ);
	// 		 AsynchronousFileChannel out = AsynchronousFileChannel.open(
	// 			 Paths.get(destFile), StandardOpenOption.CREATE, StandardOpenOption.WRITE)) {
	//
	// 		ByteBuffer buffer = ByteBuffer.allocateDirect(4 * 1024 * 1024);
	// 		CopyHandler handler = new CopyHandler(in, out, buffer);
	// 		in.read(buffer, 0, null, handler);
	// 		handler.getFuture().join();           // 비동기 완료 대기
	// 		Files.deleteIfExists(tempPath);
	//
	// 	} catch (Exception e) {
	// 		throw new UncheckedIOException("비동기 복사 실패", toIOE(e));
	// 	}
	//
	// 	// 2) 썸네일 생성 — CPU 풀로 분리
	// 	CompletableFuture<Void> thumbFuture = CompletableFuture.runAsync(() -> {
	// 		try {
	// 			Thumbnails.of(destFile)
	// 				.size(200, 200)
	// 				.toFile(thumbDir + "t_" + savedName);
	// 		} catch (IOException e) {
	// 			throw new RuntimeException(e);
	// 		}
	// 	}, thumbExecutor);
	//
	// 	thumbFuture.join();
	//
	// 	// 3) 엔티티 수집
	// 	String uploadUrl = finalUrl + datePath + "/" + prefix + "/" + savedName;
	// 	String thumbnailUrl = finalUrl + datePath + "/" + prefix + "/thumbnail/t_" + savedName;
	// 	Instant uploadedTime = Instant.now();
	// 	ImgBoard img = ImgBoard.builder()
	// 		.uploadPath(uploadUrl)
	// 		.thumbnailPath(thumbnailUrl)
	// 		.deletePossible(false)
	// 		.img(true)
	// 		.uploadTime(uploadedTime)
	// 		.extension(extension)
	// 		.fileName(originalFilename)
	// 		.fileUuid(uuid)
	// 		.postNo(postNo)
	// 		.build();
	//
	// 	FileInfoDTO info = FileInfoDTO.builder()
	// 		.uuid(uuid)
	// 		.extension(extension)
	// 		.uploadTime(uploadedTime)
	// 		.deletePossible(false)
	// 		.build();
	//
	// 	imgQueue.add(img);
	// 	infoQueue.add(info);
	// }
	// private IOException toIOE(Throwable t) {
	// 	return t instanceof IOException
	// 		? (IOException)t
	// 		: new IOException(t);
	// }

	// 업로드된 이미지의 가로세로 정보 추출

	// 개선, 매번 업로드 시 이미지의 크기 계산으로 cpu 바운드, 클라이언트(브라우저) 에 위임
	// private int[] calcImageLength(MultipartFile multipartFile) throws IOException {
	//
	// 	int[] imagesLength = new int[2];
	//
	// 	try (InputStream inputStream = multipartFile.getInputStream()) {
	//
	// 		BufferedImage bufferedImage = ImageIO.read(inputStream);
	//
	// 		imagesLength[0] = bufferedImage.getWidth();
	// 		imagesLength[1] = bufferedImage.getHeight();
	//
	// 	} catch (IOException e) {
	// 		log.error("이미지 가로세로 추출 에러!");
	// 		throw e;
	// 	}
	// 	return imagesLength;
	// }

	// 개선 전
	// @Override
	// public String uploadSummerNoteImage(List<MultipartFile> multipartFiles, HttpServletRequest request) {
	//
	// 	JsonObject jsonObject = new JsonObject();
	// 	JsonArray jsonArray = new JsonArray();
	//
	// 	// 이미지 저장 경로 설정
	// 	String contextRoot = tempUploadPath;
	//
	// 	for (MultipartFile multipartFile : multipartFiles) {
	// 		String originalFileName = multipartFile.getOriginalFilename();
	// 		assert originalFileName != null;
	//
	// 		// 확장자 추출
	// 		String extension = originalFileName.substring(originalFileName.lastIndexOf("."));
	//
	// 		// 원본 파일 이름
	//
	// 		// UUID 파일명
	// 		String uuid = UUID.randomUUID().toString();
	// 		String saveFileName = uuid + extension;
	//
	// 		// 파일경로, 이름 정보를 저장하는 객체
	// 		File targetFile = new File(contextRoot + saveFileName);
	//
	// 		try {
	// 			InputStream fileStream = multipartFile.getInputStream();
	//
	// 			// 파일의 가로 세로 길이 정보 저장
	// 			List<Integer> imagesLength = calcImageLength(multipartFile);
	//
	// 			FileUtils.copyInputStreamToFile(fileStream, targetFile);
	//
	// 			// 이미지의 URL 생성
	// 			// String imageUrl = baseUrl + "/temp/" + saveFileName;
	// 			// 아래는 로컬용
	// 			String imageUrl = baseUrl + "/temp/" + saveFileName;
	// 			JsonObject fileJsonObject = new JsonObject();
	//
	// 			// 생성된 파일의 uuid 와 이미지링크 summernote 에 전달
	// 			fileJsonObject.addProperty("imageUrl", imageUrl);
	// 			fileJsonObject.addProperty("uuid", uuid);
	// 			fileJsonObject.addProperty("extension", extension);
	// 			fileJsonObject.addProperty("name", originalFileName);
	// 			fileJsonObject.addProperty("width", imagesLength.get(0));
	// 			fileJsonObject.addProperty("height", imagesLength.get(1));
	//
	// 			jsonArray.add(fileJsonObject);
	//
	// 		} catch (IOException e) {
	// 			// 파일 저장 중 오류가 발생한 경우 해당 파일 삭제 및 에러 응답 코드 추가
	// 			log.error("Summernote Image Upload failed", e);
	// 			FileUtils.deleteQuietly(targetFile);
	// 			JsonObject errorJsonObject = new JsonObject();
	// 			jsonObject.addProperty("responseCode", "error");
	// 			jsonArray.add(errorJsonObject);
	// 			e.printStackTrace();
	// 		}
	//
	// 	}
	// 	jsonObject.add("files", jsonArray);
	//
	// 	return jsonObject.toString();
	// }

	// summernote 로 임시파일 업로드 후 게시글 등록하면 자동 s3 업로드
	// @Override
	// public ResponseEntity<List<String>> uploadS3Image(List<MultipartFile> multipartFile, Post postNo,
	// 	List<String> uuids) {
	//
	// 	List<String> error = new ArrayList<>();
	// 	List<String> uploadResult = new ArrayList<>();
	//
	// 	if (multipartFile == null || multipartFile.isEmpty()) {
	//
	// 		log.error("업로드된 파일 없음!");
	//
	// 		error.add("업로드된 파일이 없음!");
	// 		error.add(String.valueOf(postNo));
	// 		error.add(String.valueOf(System.currentTimeMillis()));
	//
	// 		return ResponseEntity.badRequest().body(error);
	// 	}
	//
	// 	for (int i = 0; i < uuids.size(); i++) {
	//
	// 		MultipartFile file = multipartFile.get(i);
	//
	// 		String originalFileName = file.getOriginalFilename();
	// 		String fileUuid = uuids.get(i);
	//
	// 		try {
	// 			// s3업로드 실행 전에 게시글 등록 로직 먼저 실행 후 반환 되는 postNo
	// 			uploadResult = s3Uploader.upload(file, postNo, fileUuid);
	//
	// 		} catch (Exception e) {
	// 			log.error("S3 업로드 에러", e);
	// 			error.add("S3 업로드 에러: " + e.getMessage());
	// 			error.add(String.valueOf(postNo));
	// 			error.add(originalFileName);
	// 			error.add(fileUuid);
	// 			error.add(String.valueOf(System.currentTimeMillis()));
	// 			return ResponseEntity.status(500).body(error);
	// 		}
	// 	}
	//
	// 	return ResponseEntity.ok(uploadResult);
	// }
	//
	// @Override
	// @Transactional
	// public void deleteUploadedS3Image(List<String> deletedImageUrls) {
	// 	// 버킷의 업로드된 파일 제거
	// 	for (String imageUrl : deletedImageUrls) {
	// 		s3Uploader.deleteS3BucketFile(imageUrl);
	// 		uploadRepository.deleteByUploadPath(imageUrl);
	// 	}
	// }

	// 개선, 게시글 등록 시 임시 파일을 업로드 성공 폴더로 옮김
	// 처음에 쓰던 것
	// public void moveAndSaveImages(List<String> uuids, List<String> extensions, List<String> originalFileNames,
	// 	Post postNo) throws IOException {
	// 	// final 디렉터리 및 썸네일 디렉터리 보장
	//
	// 	String finalDir = finalUploadPath + datePath;
	//
	// 	List<ImgBoard> imgs = new ArrayList<>();
	//
	// 	// 개선, redis 파이프라이닝을 위해
	// 	List<FileInfoDTO> uploadedFileInfo = new ArrayList<>();
	//
	// 	List<ImgBoard> imgs = new ArrayList<>();
	// 	List<FileInfoDTO> uploadedInfos = new ArrayList<>();
	// 	List<CompletableFuture<Void>> tasks = new ArrayList<>();
	//
	// 	for (int i = 0; i < uuids.size(); i++) {
	// 		String uuid = uuids.get(i);
	// 		String ext = extensions.get(i);
	// 		String saveFileName = uuid + ext;
	// 		String originalFileName = originalFileNames.get(i);
	//
	// 		// 해싱 분산을 위한 uuid의 prefix 추출
	// 		String prefix = uuid.substring(0, 2);
	//
	// 		String thumbDir = finalDir + "/" + prefix + "/thumbnail/";
	//
	// 		Files.createDirectories(Path.of(thumbDir));
	//
	// 		// 임시 파일 경로
	// 		Path tempFilePath = Paths.get(tempUploadPath, saveFileName);
	// 		// 최종 파일 경로
	// 		String finishedFile = finalDir + "/" + prefix + "/" + saveFileName;
	//
	// 		// 파일 이동 (복사 후 원본 삭제)
	// 		Path target = Path.of(finishedFile);
	//
	// 		Files.move(tempFilePath, target);
	//
	// 		// 이동 후 썸네일 생성
	// 		String finishedThumbFile = thumbDir + "t_" + saveFileName;
	// 		Thumbnailator.createThumbnail(target.toFile(), new File(finishedThumbFile), 200, 200);
	//
	// 		String finishedPath = finalUrl + datePath + "/" + prefix + "/" + saveFileName;
	// 		String finishedThumbPath = finalUrl + datePath + "/" + prefix + "/thumbnail/t_" + saveFileName;
	//
	// 		// DB 엔티티 저장
	// 		ImgBoard img = ImgBoard.builder()
	// 			.uploadPath(finishedPath)
	// 			.thumbnailPath(finishedThumbPath)
	// 			.deletePossible(false)
	// 			.img(true)
	// 			.uploadTime(Instant.now())
	// 			.extension(ext)
	// 			.fileName(originalFileName)
	// 			.fileUuid(uuid)
	// 			.postNo(postNo)
	// 			.build();
	//
	// 		imgs.add(img);
	//
	// 		FileInfoDTO dto = FileInfoDTO.builder()
	// 			.uuid(img.getFileUuid())
	// 			.extension(img.getExtension())
	// 			.uploadTime(img.getUploadTime())
	// 			.deletePossible(img.isDeletePossible())
	// 			.build();
	//
	// 		uploadedFileInfo.add(dto);
	// 	}
	//
	// 	uploadRepository.saveAll(imgs);
	//
	// 	// 한꺼번에 모아서 캐싱
	// 	uploadedImageCaching.cacheMetadataAddOrDelete(uploadedFileInfo, new ArrayList<>());
	// }

}