package com.tinyslide.offline;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Locale;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class MainActivity extends Activity {

    private static final int CREATE_PPTX_REQUEST = 1001;

    private EditText inputText;
    private TextView previewText;
    private Button exportBtn;
    private final ArrayList<SlideItem> generatedSlides = new ArrayList<>();

    static class SlideItem {
        String title;
        ArrayList<String> bullets;
        boolean titleSlide;

        SlideItem(String title, ArrayList<String> bullets, boolean titleSlide) {
            this.title = title;
            this.bullets = bullets;
            this.titleSlide = titleSlide;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ScrollView mainScroll = new ScrollView(this);
        mainScroll.setFillViewport(true);
        mainScroll.setBackgroundColor(Color.rgb(245, 247, 251));

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(28, 28, 28, 28);

        TextView heading = new TextView(this);
        heading.setText("TinySlide Offline AI");
        heading.setTextSize(26);
        heading.setTypeface(Typeface.DEFAULT_BOLD);
        heading.setTextColor(Color.rgb(22, 37, 84));
        heading.setGravity(Gravity.CENTER);
        heading.setPadding(0, 8, 0, 8);
        root.addView(heading);

        TextView subtitle = new TextView(this);
        subtitle.setText("Generate clean slide previews and export PPTX offline");
        subtitle.setTextSize(14);
        subtitle.setTextColor(Color.rgb(82, 95, 127));
        subtitle.setGravity(Gravity.CENTER);
        subtitle.setPadding(0, 0, 0, 24);
        root.addView(subtitle);

        TextView inputLabel = sectionLabel("Input Content");
        root.addView(inputLabel);

        inputText = new EditText(this);
        inputText.setHint("Example:\nTopic: AI in Healthcare\n\nProblem:\n- Manual diagnosis is slow.\n\nSolution:\n- AI supports doctors with faster analysis.\n\nBenefits:\n- Faster diagnosis\n- Better monitoring");
        inputText.setTextSize(15);
        inputText.setTextColor(Color.rgb(25, 35, 55));
        inputText.setHintTextColor(Color.rgb(125, 135, 155));
        inputText.setMinLines(7);
        inputText.setMaxLines(10);
        inputText.setVerticalScrollBarEnabled(true);
        inputText.setGravity(Gravity.TOP);
        inputText.setPadding(22, 18, 22, 18);
        inputText.setBackground(roundedBox(Color.WHITE, Color.rgb(210, 218, 232), 18));

        LinearLayout.LayoutParams inputParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                520
        );
        inputParams.setMargins(0, 8, 0, 18);
        root.addView(inputText, inputParams);

        Button generateBtn = primaryButton("Generate Slides");
        root.addView(generateBtn);

        exportBtn = secondaryButton("Export PPTX");
        exportBtn.setEnabled(false);
        root.addView(exportBtn);

        TextView previewLabel = sectionLabel("Slides Preview");
        LinearLayout.LayoutParams labelParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        labelParams.setMargins(0, 22, 0, 8);
        root.addView(previewLabel, labelParams);

        previewText = new TextView(this);
        previewText.setTextSize(15);
        previewText.setTextColor(Color.rgb(35, 45, 65));
        previewText.setLineSpacing(6, 1.0f);
        previewText.setPadding(22, 22, 22, 22);
        previewText.setText("Generated slides preview will appear here.");
        previewText.setBackground(roundedBox(Color.WHITE, Color.rgb(225, 230, 240), 18));
        root.addView(previewText);

        mainScroll.addView(root);
        setContentView(mainScroll);

        generateBtn.setOnClickListener(v -> generateSlidesPreview());
        exportBtn.setOnClickListener(v -> exportPptx());
    }

    private TextView sectionLabel(String text) {
        TextView label = new TextView(this);
        label.setText(text);
        label.setTextSize(17);
        label.setTypeface(Typeface.DEFAULT_BOLD);
        label.setTextColor(Color.rgb(31, 52, 106));
        return label;
    }

    private Button primaryButton(String text) {
        Button button = new Button(this);
        button.setText(text);
        button.setTextSize(16);
        button.setTypeface(Typeface.DEFAULT_BOLD);
        button.setTextColor(Color.WHITE);
        button.setAllCaps(false);
        button.setBackground(roundedBox(Color.rgb(37, 99, 235), Color.rgb(37, 99, 235), 16));
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                120
        );
        params.setMargins(0, 4, 0, 12);
        button.setLayoutParams(params);
        return button;
    }

    private Button secondaryButton(String text) {
        Button button = new Button(this);
        button.setText(text);
        button.setTextSize(16);
        button.setTypeface(Typeface.DEFAULT_BOLD);
        button.setTextColor(Color.rgb(37, 99, 235));
        button.setAllCaps(false);
        button.setBackground(roundedBox(Color.WHITE, Color.rgb(37, 99, 235), 16));
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                120
        );
        params.setMargins(0, 0, 0, 8);
        button.setLayoutParams(params);
        return button;
    }

    private GradientDrawable roundedBox(int fillColor, int strokeColor, int radius) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(fillColor);
        drawable.setCornerRadius(radius);
        drawable.setStroke(2, strokeColor);
        return drawable;
    }

    private void generateSlidesPreview() {
        String text = inputText.getText().toString().trim();

        if (text.isEmpty()) {
            Toast.makeText(this, "Please enter some text first", Toast.LENGTH_SHORT).show();
            return;
        }

        generatedSlides.clear();

        String topic = extractTopic(text);
        generatedSlides.add(new SlideItem(topic, listOf("Generated by TinySlide Offline AI", "Mobile-based offline slide generation"), true));

        ArrayList<String> problems = findLines(text, new String[]{"problem", "challenge", "issue", "difficulty", "manual", "lack", "slow"});
        ArrayList<String> solutions = findLines(text, new String[]{"solution", "proposed", "system", "application", "platform", "automate", "generate", "solve"});
        ArrayList<String> benefits = findLines(text, new String[]{"benefit", "advantage", "save", "reduce", "improve", "faster", "efficient", "easy", "help"});
        ArrayList<String> process = findLines(text, new String[]{"first", "then", "next", "after", "finally", "process", "step", "workflow"});
        ArrayList<String> statistics = findLines(text, new String[]{"%", "data", "accuracy", "result", "performance", "score", "cost"});
        ArrayList<String> examples = findLines(text, new String[]{"example", "such as", "for example", "for instance", "use case"});

        ArrayList<String> overview = createOverview(text);
        if (!overview.isEmpty()) {
            generatedSlides.add(new SlideItem("Presentation Overview", overview, false));
        }

        if (!problems.isEmpty()) {
            generatedSlides.add(new SlideItem("Problem Statement", limitBullets(problems, 5), false));
        }

        if (!solutions.isEmpty()) {
            generatedSlides.add(new SlideItem("Proposed Solution", limitBullets(solutions, 5), false));
        }

        if (!benefits.isEmpty()) {
            generatedSlides.add(new SlideItem("Key Benefits", limitBullets(benefits, 5), false));
        }

        if (!process.isEmpty()) {
            generatedSlides.add(new SlideItem("Working Process", limitBullets(process, 5), false));
        }

        if (!statistics.isEmpty()) {
            generatedSlides.add(new SlideItem("Results and Statistics", limitBullets(statistics, 5), false));
        }

        if (!examples.isEmpty()) {
            generatedSlides.add(new SlideItem("Examples and Use Cases", limitBullets(examples, 5), false));
        }

        if (generatedSlides.size() <= 2) {
            generatedSlides.addAll(makeFallbackSlides(text));
        }

        generatedSlides.add(new SlideItem("Conclusion", listOf(
                "The system converts raw content into structured slide material.",
                "The Android app can preview and export slides offline.",
                "The generated PPTX can be opened in PowerPoint or WPS Office."
        ), false));

        previewText.setText(buildPreviewText());
        exportBtn.setEnabled(true);

        Toast.makeText(this, "Slides generated successfully", Toast.LENGTH_SHORT).show();
    }

    private String extractTopic(String text) {
        String[] lines = text.split("\\n");

        for (String line : lines) {
            String clean = line.trim();
            String lower = clean.toLowerCase(Locale.ROOT);

            if (lower.startsWith("topic:") || lower.startsWith("title:")) {
                return clean.substring(clean.indexOf(":") + 1).trim();
            }
        }

        for (String line : lines) {
            String clean = line.trim();
            if (clean.length() > 5 && clean.length() < 80 && !clean.startsWith("-")) {
                return clean.replace(":", "");
            }
        }

        return "Generated Presentation";
    }

    private ArrayList<String> createOverview(String text) {
        ArrayList<String> overview = new ArrayList<>();
        ArrayList<String> cleanLines = cleanContentLines(text);

        for (String line : cleanLines) {
            if (!isHeadingLine(line) && line.length() > 25) {
                overview.add(shorten(line, 120));
            }

            if (overview.size() == 3) {
                break;
            }
        }

        return overview;
    }

    private ArrayList<String> findLines(String text, String[] keywords) {
        ArrayList<String> results = new ArrayList<>();
        ArrayList<String> lines = cleanContentLines(text);

        for (String line : lines) {
            String lower = line.toLowerCase(Locale.ROOT);

            for (String key : keywords) {
                if (lower.contains(key)) {
                    String cleaned = cleanupBullet(line);
                    if (!containsIgnoreCase(results, cleaned) && cleaned.length() > 3) {
                        results.add(shorten(cleaned, 140));
                    }
                    break;
                }
            }
        }

        return results;
    }

    private ArrayList<String> cleanContentLines(String text) {
        ArrayList<String> lines = new ArrayList<>();
        String normalized = text.replace("\r", "\n");
        String[] roughParts = normalized.split("\\n|(?<=[.!?])\\s+");

        for (String part : roughParts) {
            String clean = cleanupBullet(part.trim());

            if (clean.length() < 4) {
                continue;
            }

            String lower = clean.toLowerCase(Locale.ROOT);
            if (lower.startsWith("topic:") || lower.startsWith("title:")) {
                continue;
            }

            lines.add(clean);
        }

        return lines;
    }

    private boolean isHeadingLine(String line) {
        String lower = line.toLowerCase(Locale.ROOT);
        return lower.equals("problem")
                || lower.equals("solution")
                || lower.equals("benefits")
                || lower.equals("benefit")
                || lower.equals("process")
                || lower.equals("examples")
                || lower.equals("statistics")
                || lower.endsWith(":");
    }

    private String cleanupBullet(String text) {
        return text.replaceAll("^[\\-•*\\d.)\\s]+", "").trim();
    }

    private ArrayList<String> limitBullets(ArrayList<String> source, int max) {
        ArrayList<String> result = new ArrayList<>();

        for (String item : source) {
            if (!result.contains(item)) {
                result.add(item);
            }

            if (result.size() == max) {
                break;
            }
        }

        return result;
    }

    private boolean containsIgnoreCase(ArrayList<String> list, String value) {
        for (String item : list) {
            if (item.equalsIgnoreCase(value)) {
                return true;
            }
        }

        return false;
    }

    private String shorten(String text, int maxLength) {
        if (text.length() <= maxLength) {
            return text;
        }

        return text.substring(0, maxLength - 3).trim() + "...";
    }

    private ArrayList<SlideItem> makeFallbackSlides(String text) {
        ArrayList<SlideItem> slides = new ArrayList<>();
        ArrayList<String> lines = cleanContentLines(text);

        ArrayList<String> first = new ArrayList<>();
        ArrayList<String> second = new ArrayList<>();

        for (int i = 0; i < lines.size(); i++) {
            if (i < 4) {
                first.add(shorten(lines.get(i), 140));
            } else if (i < 8) {
                second.add(shorten(lines.get(i), 140));
            }
        }

        if (!first.isEmpty()) {
            slides.add(new SlideItem("Key Points", first, false));
        }

        if (!second.isEmpty()) {
            slides.add(new SlideItem("Additional Details", second, false));
        }

        return slides;
    }

    private ArrayList<String> listOf(String... items) {
        ArrayList<String> list = new ArrayList<>();

        for (String item : items) {
            list.add(item);
        }

        return list;
    }

    private String buildPreviewText() {
        StringBuilder preview = new StringBuilder();

        preview.append("Generated Slides Preview\n");
        preview.append("━━━━━━━━━━━━━━━━━━━━━━\n\n");

        for (int i = 0; i < generatedSlides.size(); i++) {
            SlideItem slide = generatedSlides.get(i);
            preview.append("Slide ").append(i + 1).append(": ").append(slide.title).append("\n");

            for (String bullet : slide.bullets) {
                preview.append("• ").append(bullet).append("\n");
            }

            preview.append("\n");
        }

        return preview.toString();
    }

    private void exportPptx() {
        if (generatedSlides.isEmpty()) {
            Toast.makeText(this, "Generate slides first", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("application/vnd.openxmlformats-officedocument.presentationml.presentation");
        intent.putExtra(Intent.EXTRA_TITLE, "TinySlide_Generated_Presentation.pptx");
        startActivityForResult(intent, CREATE_PPTX_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode != CREATE_PPTX_REQUEST || resultCode != RESULT_OK || data == null) {
            return;
        }

        Uri uri = data.getData();

        if (uri == null) {
            Toast.makeText(this, "File location not selected", Toast.LENGTH_LONG).show();
            return;
        }

        try (OutputStream outputStream = getContentResolver().openOutputStream(uri)) {
            if (outputStream == null) {
                Toast.makeText(this, "Unable to open selected file", Toast.LENGTH_LONG).show();
                return;
            }

            createPptx(outputStream, generatedSlides);
            Toast.makeText(this, "PPTX saved successfully", Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Toast.makeText(this, "Export error: " + e.getMessage(), Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }

    private void createPptx(OutputStream outputStream, ArrayList<SlideItem> slides) throws Exception {
        try (ZipOutputStream zip = new ZipOutputStream(outputStream)) {
            add(zip, "[Content_Types].xml", contentTypes(slides.size()));
            add(zip, "_rels/.rels", rootRels());

            add(zip, "docProps/core.xml", coreXml());
            add(zip, "docProps/app.xml", appXml(slides.size()));

            add(zip, "ppt/presentation.xml", presentationXml(slides.size()));
            add(zip, "ppt/_rels/presentation.xml.rels", presentationRels(slides.size()));

            add(zip, "ppt/theme/theme1.xml", themeXml());

            add(zip, "ppt/slideMasters/slideMaster1.xml", slideMasterXml());
            add(zip, "ppt/slideMasters/_rels/slideMaster1.xml.rels", slideMasterRels());

            add(zip, "ppt/slideLayouts/slideLayout1.xml", slideLayoutXml());
            add(zip, "ppt/slideLayouts/_rels/slideLayout1.xml.rels", slideLayoutRels());

            for (int i = 0; i < slides.size(); i++) {
                int slideNumber = i + 1;
                add(zip, "ppt/slides/slide" + slideNumber + ".xml", slideXml(slides.get(i)));
                add(zip, "ppt/slides/_rels/slide" + slideNumber + ".xml.rels", slideRels());
            }
        }
    }

    private void add(ZipOutputStream zip, String path, String content) throws Exception {
        zip.putNextEntry(new ZipEntry(path));
        zip.write(content.getBytes(StandardCharsets.UTF_8));
        zip.closeEntry();
    }

    private String contentTypes(int slideCount) {
        StringBuilder sb = new StringBuilder();

        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>");
        sb.append("<Types xmlns=\"http://schemas.openxmlformats.org/package/2006/content-types\">");
        sb.append("<Default Extension=\"rels\" ContentType=\"application/vnd.openxmlformats-package.relationships+xml\"/>");
        sb.append("<Default Extension=\"xml\" ContentType=\"application/xml\"/>");
        sb.append("<Override PartName=\"/docProps/core.xml\" ContentType=\"application/vnd.openxmlformats-package.core-properties+xml\"/>");
        sb.append("<Override PartName=\"/docProps/app.xml\" ContentType=\"application/vnd.openxmlformats-officedocument.extended-properties+xml\"/>");
        sb.append("<Override PartName=\"/ppt/presentation.xml\" ContentType=\"application/vnd.openxmlformats-officedocument.presentationml.presentation.main+xml\"/>");
        sb.append("<Override PartName=\"/ppt/slideMasters/slideMaster1.xml\" ContentType=\"application/vnd.openxmlformats-officedocument.presentationml.slideMaster+xml\"/>");
        sb.append("<Override PartName=\"/ppt/slideLayouts/slideLayout1.xml\" ContentType=\"application/vnd.openxmlformats-officedocument.presentationml.slideLayout+xml\"/>");
        sb.append("<Override PartName=\"/ppt/theme/theme1.xml\" ContentType=\"application/vnd.openxmlformats-officedocument.theme+xml\"/>");

        for (int i = 1; i <= slideCount; i++) {
            sb.append("<Override PartName=\"/ppt/slides/slide")
                    .append(i)
                    .append(".xml\" ContentType=\"application/vnd.openxmlformats-officedocument.presentationml.slide+xml\"/>");
        }

        sb.append("</Types>");
        return sb.toString();
    }

    private String rootRels() {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
                + "<Relationships xmlns=\"http://schemas.openxmlformats.org/package/2006/relationships\">"
                + "<Relationship Id=\"rId1\" "
                + "Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument\" "
                + "Target=\"ppt/presentation.xml\"/>"
                + "<Relationship Id=\"rId2\" "
                + "Type=\"http://schemas.openxmlformats.org/package/2006/relationships/metadata/core-properties\" "
                + "Target=\"docProps/core.xml\"/>"
                + "<Relationship Id=\"rId3\" "
                + "Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/extended-properties\" "
                + "Target=\"docProps/app.xml\"/>"
                + "</Relationships>";
    }

    private String presentationXml(int slideCount) {
        StringBuilder sb = new StringBuilder();

        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>");
        sb.append("<p:presentation xmlns:a=\"http://schemas.openxmlformats.org/drawingml/2006/main\" ");
        sb.append("xmlns:r=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships\" ");
        sb.append("xmlns:p=\"http://schemas.openxmlformats.org/presentationml/2006/main\">");
        sb.append("<p:sldMasterIdLst>");
        sb.append("<p:sldMasterId id=\"2147483648\" r:id=\"rId1\"/>");
        sb.append("</p:sldMasterIdLst>");
        sb.append("<p:sldIdLst>");

        for (int i = 1; i <= slideCount; i++) {
            sb.append("<p:sldId id=\"")
                    .append(255 + i)
                    .append("\" r:id=\"rId")
                    .append(i + 1)
                    .append("\"/>");
        }

        sb.append("</p:sldIdLst>");
        sb.append("<p:sldSz cx=\"9144000\" cy=\"5143500\" type=\"screen4x3\"/>");
        sb.append("<p:notesSz cx=\"6858000\" cy=\"9144000\"/>");
        sb.append("<p:defaultTextStyle/>");
        sb.append("</p:presentation>");

        return sb.toString();
    }

    private String presentationRels(int slideCount) {
        StringBuilder sb = new StringBuilder();

        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>");
        sb.append("<Relationships xmlns=\"http://schemas.openxmlformats.org/package/2006/relationships\">");
        sb.append("<Relationship Id=\"rId1\" ");
        sb.append("Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/slideMaster\" ");
        sb.append("Target=\"slideMasters/slideMaster1.xml\"/>");

        for (int i = 1; i <= slideCount; i++) {
            sb.append("<Relationship Id=\"rId")
                    .append(i + 1)
                    .append("\" Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/slide\" Target=\"slides/slide")
                    .append(i)
                    .append(".xml\"/>");
        }

        sb.append("</Relationships>");
        return sb.toString();
    }

    private String slideXml(SlideItem slide) {
        String title = escapeXml(slide.title);

        if (slide.titleSlide) {
            return titleSlideXml(title, slide.bullets);
        }

        return contentSlideXml(title, slide.bullets);
    }

    private String titleSlideXml(String title, ArrayList<String> bullets) {
        String subtitle = bullets.isEmpty() ? "Generated by TinySlide Offline AI" : escapeXml(bullets.get(0));
        String note = bullets.size() > 1 ? escapeXml(bullets.get(1)) : "Offline Android PPTX Export";

        return "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
                + "<p:sld xmlns:a=\"http://schemas.openxmlformats.org/drawingml/2006/main\" "
                + "xmlns:r=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships\" "
                + "xmlns:p=\"http://schemas.openxmlformats.org/presentationml/2006/main\">"
                + "<p:cSld><p:bg><p:bgPr><a:solidFill><a:srgbClr val=\"EFF6FF\"/></a:solidFill></p:bgPr></p:bg>"
                + "<p:spTree>"
                + groupShape()
                + rectShape(10, 0, 0, 9144000, 5143500, "EFF6FF")
                + rectShape(11, 0, 0, 9144000, 520000, "2563EB")
                + textShape(2, "Title", 800000, 1450000, 7600000, 900000, title, 4200, true, false)
                + textShape(3, "Subtitle", 1200000, 2450000, 6900000, 500000, subtitle, 2200, false, false)
                + textShape(4, "Note", 1200000, 3150000, 6900000, 500000, note, 1800, false, false)
                + "</p:spTree></p:cSld>"
                + "<p:clrMapOvr><a:masterClrMapping/></p:clrMapOvr>"
                + "</p:sld>";
    }

    private String contentSlideXml(String title, ArrayList<String> bullets) {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
                + "<p:sld xmlns:a=\"http://schemas.openxmlformats.org/drawingml/2006/main\" "
                + "xmlns:r=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships\" "
                + "xmlns:p=\"http://schemas.openxmlformats.org/presentationml/2006/main\">"
                + "<p:cSld><p:bg><p:bgPr><a:solidFill><a:srgbClr val=\"FFFFFF\"/></a:solidFill></p:bgPr></p:bg>"
                + "<p:spTree>"
                + groupShape()
                + rectShape(10, 0, 0, 9144000, 420000, "2563EB")
                + rectShape(11, 620000, 1180000, 7900000, 2600000, "F8FAFC")
                + textShape(2, "Title", 650000, 560000, 7900000, 620000, title, 3200, true, false)
                + bulletShape(3, "Bullets", 950000, 1420000, 7300000, 2350000, bullets)
                + textShape(4, "Footer", 650000, 4600000, 7900000, 250000, "TinySlide Offline AI • Generated on mobile", 1100, false, false)
                + "</p:spTree></p:cSld>"
                + "<p:clrMapOvr><a:masterClrMapping/></p:clrMapOvr>"
                + "</p:sld>";
    }

    private String groupShape() {
        return "<p:nvGrpSpPr>"
                + "<p:cNvPr id=\"1\" name=\"\"/>"
                + "<p:cNvGrpSpPr/>"
                + "<p:nvPr/>"
                + "</p:nvGrpSpPr>"
                + "<p:grpSpPr>"
                + "<a:xfrm>"
                + "<a:off x=\"0\" y=\"0\"/>"
                + "<a:ext cx=\"0\" cy=\"0\"/>"
                + "<a:chOff x=\"0\" y=\"0\"/>"
                + "<a:chExt cx=\"0\" cy=\"0\"/>"
                + "</a:xfrm>"
                + "</p:grpSpPr>";
    }

    private String rectShape(int id, int x, int y, int width, int height, String color) {
        return "<p:sp>"
                + "<p:nvSpPr><p:cNvPr id=\"" + id + "\" name=\"Rectangle\"/><p:cNvSpPr/><p:nvPr/></p:nvSpPr>"
                + "<p:spPr>"
                + "<a:xfrm><a:off x=\"" + x + "\" y=\"" + y + "\"/><a:ext cx=\"" + width + "\" cy=\"" + height + "\"/></a:xfrm>"
                + "<a:prstGeom prst=\"rect\"><a:avLst/></a:prstGeom>"
                + "<a:solidFill><a:srgbClr val=\"" + color + "\"/></a:solidFill>"
                + "<a:ln><a:noFill/></a:ln>"
                + "</p:spPr>"
                + "</p:sp>";
    }

    private String textShape(int id, String name, int x, int y, int width, int height, String text, int fontSize, boolean bold, boolean center) {
        return "<p:sp>"
                + "<p:nvSpPr><p:cNvPr id=\"" + id + "\" name=\"" + name + "\"/><p:cNvSpPr/><p:nvPr/></p:nvSpPr>"
                + "<p:spPr>"
                + "<a:xfrm><a:off x=\"" + x + "\" y=\"" + y + "\"/><a:ext cx=\"" + width + "\" cy=\"" + height + "\"/></a:xfrm>"
                + "</p:spPr>"
                + "<p:txBody>"
                + "<a:bodyPr wrap=\"square\" anchor=\"mid\"/>"
                + "<a:lstStyle/>"
                + "<a:p" + (center ? " algn=\"ctr\"" : "") + ">"
                + "<a:r>"
                + "<a:rPr lang=\"en-US\" sz=\"" + fontSize + "\"" + (bold ? " b=\"1\"" : "") + ">"
                + "<a:solidFill><a:srgbClr val=\"111827\"/></a:solidFill>"
                + "</a:rPr>"
                + "<a:t>" + text + "</a:t>"
                + "</a:r>"
                + "</a:p>"
                + "</p:txBody>"
                + "</p:sp>";
    }

    private String bulletShape(int id, String name, int x, int y, int width, int height, ArrayList<String> bullets) {
        StringBuilder sb = new StringBuilder();

        sb.append("<p:sp>");
        sb.append("<p:nvSpPr><p:cNvPr id=\"").append(id).append("\" name=\"").append(name).append("\"/><p:cNvSpPr/><p:nvPr/></p:nvSpPr>");
        sb.append("<p:spPr>");
        sb.append("<a:xfrm><a:off x=\"").append(x).append("\" y=\"").append(y).append("\"/><a:ext cx=\"").append(width).append("\" cy=\"").append(height).append("\"/></a:xfrm>");
        sb.append("</p:spPr>");
        sb.append("<p:txBody><a:bodyPr wrap=\"square\"/><a:lstStyle/>");

        for (String bullet : bullets) {
            sb.append("<a:p>");
            sb.append("<a:pPr marL=\"320000\" indent=\"-220000\"><a:buChar char=\"•\"/></a:pPr>");
            sb.append("<a:r><a:rPr lang=\"en-US\" sz=\"2000\">");
            sb.append("<a:solidFill><a:srgbClr val=\"1F2937\"/></a:solidFill>");
            sb.append("</a:rPr><a:t>");
            sb.append(escapeXml(bullet));
            sb.append("</a:t></a:r>");
            sb.append("</a:p>");
        }

        sb.append("</p:txBody></p:sp>");
        return sb.toString();
    }

    private String slideRels() {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
                + "<Relationships xmlns=\"http://schemas.openxmlformats.org/package/2006/relationships\">"
                + "<Relationship Id=\"rId1\" "
                + "Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/slideLayout\" "
                + "Target=\"../slideLayouts/slideLayout1.xml\"/>"
                + "</Relationships>";
    }

    private String slideMasterXml() {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
                + "<p:sldMaster xmlns:a=\"http://schemas.openxmlformats.org/drawingml/2006/main\" "
                + "xmlns:r=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships\" "
                + "xmlns:p=\"http://schemas.openxmlformats.org/presentationml/2006/main\">"
                + "<p:cSld><p:spTree>"
                + groupShape()
                + "</p:spTree></p:cSld>"
                + "<p:clrMap bg1=\"lt1\" tx1=\"dk1\" bg2=\"lt2\" tx2=\"dk2\" "
                + "accent1=\"accent1\" accent2=\"accent2\" accent3=\"accent3\" "
                + "accent4=\"accent4\" accent5=\"accent5\" accent6=\"accent6\" "
                + "hlink=\"hlink\" folHlink=\"folHlink\"/>"
                + "<p:sldLayoutIdLst><p:sldLayoutId id=\"2147483649\" r:id=\"rId1\"/></p:sldLayoutIdLst>"
                + "<p:txStyles><p:titleStyle/><p:bodyStyle/><p:otherStyle/></p:txStyles>"
                + "</p:sldMaster>";
    }

    private String slideMasterRels() {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
                + "<Relationships xmlns=\"http://schemas.openxmlformats.org/package/2006/relationships\">"
                + "<Relationship Id=\"rId1\" "
                + "Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/slideLayout\" "
                + "Target=\"../slideLayouts/slideLayout1.xml\"/>"
                + "<Relationship Id=\"rId2\" "
                + "Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/theme\" "
                + "Target=\"../theme/theme1.xml\"/>"
                + "</Relationships>";
    }

    private String slideLayoutXml() {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
                + "<p:sldLayout xmlns:a=\"http://schemas.openxmlformats.org/drawingml/2006/main\" "
                + "xmlns:r=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships\" "
                + "xmlns:p=\"http://schemas.openxmlformats.org/presentationml/2006/main\" "
                + "type=\"blank\" preserve=\"1\">"
                + "<p:cSld name=\"Blank\"><p:spTree>"
                + groupShape()
                + "</p:spTree></p:cSld>"
                + "<p:clrMapOvr><a:masterClrMapping/></p:clrMapOvr>"
                + "</p:sldLayout>";
    }

    private String slideLayoutRels() {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
                + "<Relationships xmlns=\"http://schemas.openxmlformats.org/package/2006/relationships\">"
                + "<Relationship Id=\"rId1\" "
                + "Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/slideMaster\" "
                + "Target=\"../slideMasters/slideMaster1.xml\"/>"
                + "</Relationships>";
    }

    private String themeXml() {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
                + "<a:theme xmlns:a=\"http://schemas.openxmlformats.org/drawingml/2006/main\" name=\"TinySlide Theme\">"
                + "<a:themeElements>"
                + "<a:clrScheme name=\"TinySlide\">"
                + "<a:dk1><a:srgbClr val=\"111827\"/></a:dk1>"
                + "<a:lt1><a:srgbClr val=\"FFFFFF\"/></a:lt1>"
                + "<a:dk2><a:srgbClr val=\"1F2937\"/></a:dk2>"
                + "<a:lt2><a:srgbClr val=\"EFF6FF\"/></a:lt2>"
                + "<a:accent1><a:srgbClr val=\"2563EB\"/></a:accent1>"
                + "<a:accent2><a:srgbClr val=\"0EA5E9\"/></a:accent2>"
                + "<a:accent3><a:srgbClr val=\"22C55E\"/></a:accent3>"
                + "<a:accent4><a:srgbClr val=\"F59E0B\"/></a:accent4>"
                + "<a:accent5><a:srgbClr val=\"8B5CF6\"/></a:accent5>"
                + "<a:accent6><a:srgbClr val=\"EF4444\"/></a:accent6>"
                + "<a:hlink><a:srgbClr val=\"2563EB\"/></a:hlink>"
                + "<a:folHlink><a:srgbClr val=\"7C3AED\"/></a:folHlink>"
                + "</a:clrScheme>"
                + "<a:fontScheme name=\"Aptos\"><a:majorFont><a:latin typeface=\"Aptos Display\"/></a:majorFont><a:minorFont><a:latin typeface=\"Aptos\"/></a:minorFont></a:fontScheme>"
                + "<a:fmtScheme name=\"TinySlide\"><a:fillStyleLst><a:solidFill><a:schemeClr val=\"phClr\"/></a:solidFill></a:fillStyleLst><a:lnStyleLst><a:ln w=\"6350\"><a:solidFill><a:schemeClr val=\"phClr\"/></a:solidFill></a:ln></a:lnStyleLst><a:effectStyleLst><a:effectStyle/></a:effectStyleLst><a:bgFillStyleLst><a:solidFill><a:schemeClr val=\"phClr\"/></a:solidFill></a:bgFillStyleLst></a:fmtScheme>"
                + "</a:themeElements>"
                + "</a:theme>";
    }

    private String coreXml() {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
                + "<cp:coreProperties xmlns:cp=\"http://schemas.openxmlformats.org/package/2006/metadata/core-properties\" "
                + "xmlns:dc=\"http://purl.org/dc/elements/1.1/\" "
                + "xmlns:dcterms=\"http://purl.org/dc/terms/\" "
                + "xmlns:dcmitype=\"http://purl.org/dc/dcmitype/\" "
                + "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">"
                + "<dc:title>TinySlide Generated Presentation</dc:title>"
                + "<dc:creator>TinySlide Offline AI</dc:creator>"
                + "</cp:coreProperties>";
    }

    private String appXml(int slidesCount) {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
                + "<Properties xmlns=\"http://schemas.openxmlformats.org/officeDocument/2006/extended-properties\" "
                + "xmlns:vt=\"http://schemas.openxmlformats.org/officeDocument/2006/docPropsVTypes\">"
                + "<Application>TinySlide Offline AI</Application>"
                + "<PresentationFormat>On-screen Show</PresentationFormat>"
                + "<Slides>" + slidesCount + "</Slides>"
                + "</Properties>";
    }

    private String escapeXml(String text) {
        if (text == null) {
            return "";
        }

        return text.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&apos;");
    }
}
