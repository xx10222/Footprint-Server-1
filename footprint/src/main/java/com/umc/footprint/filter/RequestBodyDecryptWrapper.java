package com.umc.footprint.filter;

import com.amazonaws.util.IOUtils;
import com.umc.footprint.config.EncryptProperties;
import com.umc.footprint.utils.AES128;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;

import javax.servlet.ReadListener;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.io.*;
import java.nio.charset.StandardCharsets;

@Slf4j
public class RequestBodyDecryptWrapper extends HttpServletRequestWrapper {
    // 가로챈 데이터를 가공하여 담을 final 변수
    private final String requestDecryptBody;
    private final EncryptProperties encryptProperties;

    public RequestBodyDecryptWrapper(HttpServletRequest request, EncryptProperties encryptProperties) throws IOException, DecoderException {
        super(request);
        this.encryptProperties = encryptProperties;

        String requestHashData = requestDataByte(request); // Request Data 가로채기
        String decodeTemp = requestBodyDecode(requestHashData); // Request Data AES128 디코드

        log.info("인코딩 데이터: " + requestHashData);
        log.info("디코딩 데이터: " + decodeTemp);

        requestDecryptBody = decodeTemp;
    }

    @Override
    public ServletInputStream getInputStream() {
        final ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(requestDecryptBody.getBytes(StandardCharsets.UTF_8));
        return new ServletInputStream() {
            @Override
            public boolean isFinished() {
                return false;
            }

            @Override
            public boolean isReady() {
                return false;
            }

            @Override
            public void setReadListener(ReadListener listener) {
            }

            @Override
            public int read() {
                return byteArrayInputStream.read();
            }
        };
    }

    @Override
    public BufferedReader getReader() {
        return new BufferedReader(new InputStreamReader(this.getInputStream()));
    }

    // -- request Body 가져오기 --
    private String requestDataByte(HttpServletRequest request) throws IOException {
        byte[] rawData;
        InputStream inputStream = request.getInputStream();

        rawData = IOUtils.toByteArray(inputStream);

        return new String(rawData);
    }

    // -- request Body AES128 디코딩 --
    private String requestBodyDecode(String requestHashData) throws DecoderException {

        try{
            // String decryptedImageUrl = new AES128(encryptProperties.getKey()).decrypt(imageUrl);
            String decryptResult = new AES128(encryptProperties.getKey()).decrypt(requestHashData);

            return decryptResult;
        } catch (Exception exception){
            throw new DecoderException();
        }
    }

}