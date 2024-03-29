package com.maveric.hr360.service.implementation;

import com.maveric.hr360.Exception.CustomException;
import com.maveric.hr360.service.EmployeeService;
import com.mongodb.client.gridfs.GridFSBucket;
import com.mongodb.client.gridfs.GridFSDownloadStream;
import com.mongodb.client.gridfs.GridFSUploadStream;
import com.mongodb.client.gridfs.model.GridFSFile;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StreamUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipInputStream;

import static com.mongodb.client.model.Filters.eq;

@Service
@Slf4j
@RequiredArgsConstructor
public class EmployeeServiceImpl implements EmployeeService {
    private final GridFSBucket gridFSBucket;

    @Override
    public Map<String, ArrayList<String>> saveImageZip(MultipartFile file) throws IOException {
        log.info("EmployeeServiceImpl::saveImageZip:: method call started");
        Map<String, ArrayList<String>> response = new HashMap<>();
        ArrayList<String> successList=new ArrayList<>();
        ArrayList<String> failedList=new ArrayList<>();

        String originalFilename = file.getOriginalFilename();
        if (originalFilename != null && originalFilename.endsWith(".zip")) {
            try (InputStream inputStream = file.getInputStream();
                 ZipInputStream zipInputStream = new ZipInputStream(inputStream)) {
                ZipEntry entry = zipInputStream.getNextEntry();
                if (entry == null) {
                    log.error("Invalid zip file {}", originalFilename);
                    throw new ZipException("Invalid zip file: No entries found.");
                }
                while (entry != null) {
                    String fileName = entry.getName();
                    if (!entry.isDirectory()) {
                        if (fileName.toLowerCase().endsWith(".png") || fileName.toLowerCase().endsWith(".jpg")) {
                            try {
                                GridFSUploadStream uploadStream = gridFSBucket.openUploadStream(fileName.substring(0,7));
                                byte[] buffer = new byte[1024];
                                int len;
                                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                                while ((len = zipInputStream.read(buffer)) > 0) {
                                    outputStream.write(buffer, 0, len);
                                }
                                uploadStream.write(outputStream.toByteArray());
                                uploadStream.close();
                                outputStream.close();
                                successList.add(fileName);
                                log.info("Upload successful: {}", fileName);
                            } catch (Exception e) {
                                log.error("Error while uploading file: {}", fileName, e);
                                failedList.add(fileName);
                            }
                        } else {
                            log.info("File is not PNG or JPG type: {}", fileName);
                            failedList.add(fileName);
                        }
                    }
                    zipInputStream.closeEntry();
                    entry = zipInputStream.getNextEntry();
                }
                zipInputStream.close();
                inputStream.close();
            } catch (ZipException e) {
                log.error("Zip exception, Invalid zip file: {}", originalFilename, e);
                throw new ZipException("Invalid zip file");
            } catch (IOException e) {
                log.error("IO exception, file upload failed", e);
                throw new IOException("Upload failed", e);
            }
        } else {
            log.info("Uploaded file is not a zip file: {}", originalFilename);
            failedList.add( "Please upload a zip file.");
        }
        response.put("success", successList);
        response.put("error", failedList);
        log.info("EmployeeServiceImpl::saveImageZip:: method call ended");
        return response;
    }


    @Override
    public byte[] getImageByEmployeeId(String employeeId) throws IOException {
        log.info(" EmployeeServiceImpl ::getImageByEmployeeId:: method call started");
        try {
            GridFSFile gridFSFile = gridFSBucket.find(eq("filename", employeeId)).first();
            if (gridFSFile == null) {
                log.error("File not found with employee ID:{}", employeeId);
                throw new FileNotFoundException("File not found with employee ID: " + employeeId);
            }
            GridFSDownloadStream downloadStream = gridFSBucket.openDownloadStream(gridFSFile.getObjectId());
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            StreamUtils.copy(downloadStream, outputStream);
            downloadStream.close();
            byte[] bytes = outputStream.toByteArray();
            /*log.info("output stream converted to bytearray {}", bytes);*/
            log.info(" EmployeeServiceImpl ::getImageByEmployeeId:: method call ended");
            return bytes;
        } catch (IOException e) {
            log.error("Error occurred while retrieving image ");
            throw new CustomException( "Error occurred while retrieving image " ,HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }

}
