package com.tsukemendog.openbankinglink.controller;

import com.tsukemendog.openbankinglink.dto.TestDto;
import com.tsukemendog.openbankinglink.entity.RssFeed;
import com.tsukemendog.openbankinglink.repository.RssFeedRepository;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameter;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.*;

import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3AsyncClient;
import software.amazon.awssdk.transfer.s3.S3TransferManager;
import software.amazon.awssdk.transfer.s3.model.CompletedFileUpload;
import software.amazon.awssdk.transfer.s3.model.FileUpload;
import software.amazon.awssdk.transfer.s3.model.UploadFileRequest;
import software.amazon.awssdk.transfer.s3.progress.LoggingTransferListener;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;



@RestController
public class TestController {

    @Autowired
    private RssFeedRepository rssFeedRepository;

    @Autowired
    private Job job;

    @Autowired
    private JobLauncher jobLauncher;

    @GetMapping("/test")
    public Map<String, Object> greeting(@RequestParam("testID") String testId, @RequestBody TestDto testDto) {

        return Collections.singletonMap("message", testDto.getKey1() + " " + testDto.getKey2());
    }

    @GetMapping("/rss/{code}")
    public ResponseEntity<String> getFeed(@PathVariable String code) {
        Optional<RssFeed> rssFeed = Optional.empty();
        if ("movie".equals(code)) {
            rssFeed = rssFeedRepository.findByCode("movie");
        }
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        return new ResponseEntity<>(rssFeed.map(RssFeed::getContent).orElse("Empty"), headers, HttpStatus.OK);
    }

    @GetMapping("/invokejob")
    public String handle() throws Exception {
        Map<String, JobParameter> confMap = new HashMap<>();
        confMap.put("time", new JobParameter(System.currentTimeMillis()));
        jobLauncher.run(job, new JobParameters(confMap));
        return "Batch job has been invoked";
    }

    @PostMapping("/upload")
    public ResponseEntity<?> uploadChunk(
            @RequestHeader("Chunk-Start") Long chunkStart,
            @RequestHeader("Chunk-End") Long chunkEnd,
            @RequestHeader("File-Name") String fileName,
            @RequestHeader("Last-Chunk") Boolean isLastChunk,
            @RequestBody byte[] chunkData) {

        // 이 부분에 청크를 파일로 저장하거나 원하는 처리를 수행할 수 있습니다.
        try {
            // 청크를 지정된 파일에 추가 모드로 기록합니다.
            Path filePath = Paths.get("C:\\Users\\mokai\\OneDrive\\Desktop\\video-project\\test-video", fileName); // 업로드
                                                                                                                   // 디렉토리
                                                                                                                   // 지정
            FileOutputStream outputStream = new FileOutputStream(filePath.toString(), true);
            outputStream.write(chunkData);
            outputStream.close();

            if (isLastChunk) {

                // 디렉토리 경로 설정
                Path directoryPath = Paths.get("C:\\Users\\mokai\\OneDrive\\Desktop\\video-project\\test-video");

                // 디렉토리 내의 파일 수를 카운트
                long count = countFilesInDirectory(directoryPath);
                System.out.println("chunk count : " + count);

                List<Path> paths = new ArrayList<>();
                for (int i = 0; i < count; i++) {
                    File file = new File(
                            "C:\\Users\\mokai\\OneDrive\\Desktop\\video-project\\test-video\\hisense-microcosmic-ecology-uhd-(www.demolandia.net) (1).mkv"
                                    + "_chunk_" + i);
                    if (file.exists()) {
                        paths.add(Paths.get(
                                "C:\\Users\\mokai\\OneDrive\\Desktop\\video-project\\test-video\\hisense-microcosmic-ecology-uhd-(www.demolandia.net) (1).mkv"
                                        + "_chunk_" + i));
                    } else {
                        break;
                    }
                }

                Path outputPath = Paths.get(
                        "C:\\Users\\mokai\\OneDrive\\Desktop\\video-project\\test-video\\hisense-microcosmic-ecology-uhd-(www.demolandia.net).mkv");
                mergeFiles(paths, outputPath);
            }

        } catch (IOException e) {
            e.printStackTrace();
            return new ResponseEntity<>("Failed to save chunk", HttpStatus.INTERNAL_SERVER_ERROR);
        }

        // 성공적인 응답을 반환합니다.
        return new ResponseEntity<>("Chunk uploaded successfully", HttpStatus.OK);
    }

    @PostMapping("/upload2")
    public ResponseEntity<String> fileUploadTest2(@RequestParam("file") MultipartFile multipartFile) {

        System.out.println("file size : " + multipartFile.getSize());

        return ResponseEntity.ok("ok");
    }

    @GetMapping("/merge")
    public ResponseEntity<String> merge() throws IOException {

        List<Path> paths = new ArrayList<>();

        for (int i = 0; i < 500; i++) {
            File file = new File(
                    "C:\\Users\\mokai\\OneDrive\\Desktop\\video-project\\test-video\\hisense-microcosmic-ecology-uhd-(www.demolandia.net) (1).mkv"
                            + "_chunk_" + i);
            if (file.exists()) {
                paths.add(Paths.get(
                        "C:\\Users\\mokai\\OneDrive\\Desktop\\video-project\\test-video\\hisense-microcosmic-ecology-uhd-(www.demolandia.net) (1).mkv"
                                + "_chunk_" + i));
            } else {
                break;
            }
        }

        Path outputPath = Paths.get(
                "C:\\Users\\mokai\\OneDrive\\Desktop\\video-project\\test-video\\hisense-microcosmic-ecology-uhd-(www.demolandia.net).mkv");
        mergeFiles(paths, outputPath);

        return ResponseEntity.ok("병합완료");

    }

    /**
     * 여러 파일을 하나의 파일로 병합합니다.
     * 
     * @param chunkPaths 병합할 파일들의 경로 리스트
     * @param outputPath 결과 파일의 경로
     * @throws IOException 파일 I/O 중 발생하는 예외를 처리합니다.
     */
    public static void mergeFiles(List<Path> chunkPaths, Path outputPath) throws IOException {
        try (FileChannel outputChannel = FileChannel.open(outputPath, StandardOpenOption.CREATE,
                StandardOpenOption.WRITE)) {
            for (Path chunkPath : chunkPaths) {
                try (FileInputStream inputStream = new FileInputStream(chunkPath.toFile())) {
                    FileChannel inputChannel = inputStream.getChannel();
                    inputChannel.transferTo(0, inputChannel.size(), outputChannel);
                }
            }
        }
    }

    public static long countFilesInDirectory(Path directory) throws IOException {
        if (!Files.isDirectory(directory)) {
            throw new IllegalArgumentException("입력된 경로는 디렉토리가 아닙니다.");
        }

        // 디렉토리 내의 파일 수를 카운트
        long count = 0;

        try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(directory)) {
            for (Path file : directoryStream) {
                if (Files.isRegularFile(file)) {
                    count++;
                }
            }
        }

        return count;
    }



    @GetMapping("/s3")
    public ResponseEntity<String> s3UploadTest() {

        uploadFile(getS3TransferManager(), "pizzadog-video-s3-bucket", UUID.randomUUID().toString(), "C:\\Users\\mokai\\OneDrive\\Desktop\\블로그이미지\\image.png");

        return ResponseEntity.ok("ok");
    }




    public String uploadFile(S3TransferManager transferManager, String bucketName,
                             String key, String filePath) {
        UploadFileRequest uploadFileRequest =
            UploadFileRequest.builder()
                .putObjectRequest(b -> b.bucket(bucketName).key(key))
                .addTransferListener(LoggingTransferListener.create())
                .source(Paths.get(filePath))
                .build();

        FileUpload fileUpload = transferManager.uploadFile(uploadFileRequest);

        CompletedFileUpload uploadResult = fileUpload.completionFuture().join();
        return uploadResult.response().eTag();
    }


    public S3TransferManager getS3TransferManager() {
        S3AsyncClient s3AsyncClient = S3AsyncClient.builder()
                .region(Region.AP_NORTHEAST_2) // AWS 리전 설정
                .credentialsProvider(DefaultCredentialsProvider.create())
                .build();

        return S3TransferManager.builder()
                .s3Client(s3AsyncClient)
                .build();
    }


}
