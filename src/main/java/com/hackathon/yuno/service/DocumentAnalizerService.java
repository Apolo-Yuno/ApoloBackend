package com.hackathon.yuno.service;

import java.io.IOException;
import java.io.InputStream;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Service;

@Service
public class DocumentAnalizerService {

    public String extractTextFromPDFStream(InputStream pdfStream){
        try{
            byte[] bytes = pdfStream.readAllBytes();

            try(PDDocument document = Loader.loadPDF(bytes)){
                PDFTextStripper stripper = new PDFTextStripper();
                return stripper.getText(document);
            }
        } catch (IOException e){
            e.printStackTrace();
            return "Error reading PDF attachment";
        }
    }    
}
