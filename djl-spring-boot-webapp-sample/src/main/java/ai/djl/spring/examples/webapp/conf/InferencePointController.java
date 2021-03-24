package ai.djl.spring.examples.webapp.conf;

import ai.djl.inference.Predictor;
import ai.djl.modality.cv.Image;
import ai.djl.modality.cv.ImageFactory;
import ai.djl.modality.cv.output.DetectedObjects;
import ai.djl.spring.examples.webapp.data.pojo.InferenceResponse;
import ai.djl.spring.examples.webapp.data.pojo.InferredObject;
import ai.djl.translate.TranslateException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.imageio.ImageIO;

import java.awt.image.RenderedImage;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.function.Supplier;

@RestController
@RequestMapping(value = "/inference")
public class InferencePointController {

    private static final Logger logger = LoggerFactory.getLogger(InferencePointController.class);
    private static final String API_VERSION = "0.1.0";
    private static final String PNG = ".png";

    public static String getApiVersion() {
        return API_VERSION;
    }

    @Resource
    private Supplier<Predictor<Image, DetectedObjects>> predictorSupplier;

    @Resource
    private ImageFactory imageFactory;

    @GetMapping
    @RequestMapping("/version")
    public String version(){
        return getApiVersion();
    }

    @GetMapping
    @RequestMapping("/file")
    public InferenceResponse detectFromFile(@RequestParam(name = "file") String fileName,
                                    @RequestParam(name = "generateOutputImage") Boolean generateOutputImage)
            throws IOException, TranslateException {
        logger.info("/file endpoint with filename={} and generateOutputImage={}", fileName, generateOutputImage);
        String resourceRoot = "src/main/resources";
        Image image = imageFactory.fromFile(Paths.get(resourceRoot, fileName));
        return inference(image, fileName, generateOutputImage);
    }

    @GetMapping
    @RequestMapping("/url")
    public InferenceResponse detectFromUrl(@RequestParam(name = "url") String url,
                                    @RequestParam(name = "generateOutputImage") Boolean generateOutputImage)
            throws IOException, TranslateException {
        logger.info("/url endpoint with url={} and generateOutputImage={}", url, generateOutputImage);
        Image image = imageFactory.fromUrl(url);
        String fileName = extractFilenameFromUrl(url);
        return inference(image, fileName, generateOutputImage);
    }

    private String extractFilenameFromUrl(String url) {
        var spl = url.split("/");
        return spl[spl.length-1];
    }

    private InferenceResponse inference(Image image, String fileName, Boolean generateOutputImage) throws TranslateException {
        var inferredObjects = new LinkedList<InferredObject>();

        var outputFolder = "output/";
        var outputReference = "";

        try(var p = predictorSupplier.get()) {
            var detected = p.predict(image);
            if(generateOutputImage != null && generateOutputImage) {
                RenderedImage newImage = createImage(detected, image);
                outputReference = createImageFile(newImage, outputFolder.concat(fileName.concat(PNG)));
            }
            detected.items().forEach(e -> inferredObjects.add(new InferredObject(e.getClassName(), e.getProbability())));
            logger.info("Detected objects={}", inferredObjects);
            return new InferenceResponse(inferredObjects, outputReference);
        }
    }

    private String createImageFile(RenderedImage image, String filename) {
        try(OutputStream os = new FileOutputStream(filename)) {
            ImageIO.write(image, "png", os);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return filename;
    }

    private static RenderedImage createImage(DetectedObjects detection, Image original) {
        Image newImage = original.duplicate(Image.Type.TYPE_INT_ARGB);
        newImage.drawBoundingBoxes(detection);
        return (RenderedImage) newImage.getWrappedImage();
    }

}