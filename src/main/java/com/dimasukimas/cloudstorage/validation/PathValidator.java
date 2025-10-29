package com.dimasukimas.cloudstorage.validation;

import com.dimasukimas.cloudstorage.config.StorageProperties;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.RequiredArgsConstructor;

import java.util.regex.Pattern;

@RequiredArgsConstructor
public class PathValidator implements ConstraintValidator<SafePath, String> {

    private final StorageProperties storageProperties;
    private Pattern segmentPattern;

    @Override
    public void initialize(SafePath constraintAnnotation) {
        this.segmentPattern = Pattern.compile("^[\\p{L}\\p{N}._ -]+$");
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {

        if (value == null || value.isEmpty()) return true;
        if (value.startsWith(storageProperties.getDirectorySplitter())) return false;
        if (value.length() > storageProperties.getMaxPathLength()) return false;

        String[] segments = value.split(storageProperties.getDirectorySplitter());

        for (String seg : segments) {
            if (seg == null || seg.isBlank()) return false;
            if (seg.equals(".") || seg.equals("..")) return false;
            if (!segmentPattern.matcher(seg).matches()) return false;
            if (seg.startsWith(" ") || seg.endsWith(" ")) return false;
        }

        return true;
    }
}
