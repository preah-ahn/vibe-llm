package io.vibe.llm.common.engine.helper.model;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
//import com.google.common.collect.ImmutableSet;
//import kr.co.genie.cms.common.engine.constant.Constant;
import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Component;
//import org.springframework.test.web.servlet.ResultActions;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @since       2026.10.01
 * @author      preah
 * @description object helper
 **********************************************************************************************************************/
@Component
public class ObjectHelper {

	@Autowired
	private ObjectHelper(ObjectMapper objectMapper) {
		ObjectHelper.objectMapper = objectMapper;
	}

	@SneakyThrows
	public static String toJson(Object o1){
		return objectMapper.writeValueAsString(o1);
	}

	@SneakyThrows
	public static <T> T toInstance(Class<T> clazz, String json){
		return StringUtils.isNotBlank(json) ? objectMapper.readValue(json, clazz) : null;
	}

//	@SneakyThrows
//	public static <T> T toInstance(Class<T> clazz, ResultActions resultActions){
//		return toInstance(clazz, resultActions.andReturn().getResponse().getContentAsString());
//	}
//
//	public static <T> T convertValue(Object fromValue, Class<T> toValueType) {
//        return fromValue != null ? objectMapper.convertValue(fromValue, toValueType) : null;
//    }
//
//	@SneakyThrows
//	public static List toCollection(Class clazz, ResultActions resultActions) {
//		return objectMapper.readValue(resultActions.andReturn().getResponse().getContentAsString(), objectMapper.getTypeFactory().constructCollectionType(List.class, clazz));
//	}

	@SneakyThrows
	public static Map<String, Object> toMap(Object value){
		    return value != null
            ? objectMapper.convertValue(
                    value,
                    new TypeReference<Map<String, Object>>() {}
            )
            : null;
	}

	@SneakyThrows
    public static Map<String, Object> toMap(String json) {
        return StringUtils.isNotBlank(json)
            ? objectMapper.readValue(json, new TypeReference<Map<String, Object>>() {})
            : null;
    }

//	public static <T> MultiValueMap<String, String> newMultiValueMap(T object) {
//		MultiValueMap<String, String> parameters = new LinkedMultiValueMap();
//		recursive(object, parameters, Constant.String.EMPTY);
//		return parameters;
//	}

	@SneakyThrows
	public static <T> T newInstance(Class<T> clazz) {
		T t = clazz.newInstance();
		for(Field field : t.getClass().getDeclaredFields()) {
			field.setAccessible(true);
				 if(Long.class          == field.getType()) { field.set(t, NumberUtils.LONG_ZERO);    }
			else if(Integer.class       == field.getType()) { field.set(t, NumberUtils.INTEGER_ZERO); }
			else if(BigDecimal.class    == field.getType()) { field.set(t, BigDecimal.ZERO);          }
			else if(Boolean.class       == field.getType()) { field.set(t, Boolean.TRUE);             }
			else if(String.class        == field.getType()) { field.set(t, field.getName());          }
			else if(LocalTime.class     == field.getType()) { field.set(t, LocalTime.now());          }
			else if(LocalDate.class     == field.getType()) { field.set(t, LocalDate.now());          }
			else if(LocalDateTime.class == field.getType()) { field.set(t, LocalDateTime.now());      }
			else if(field.getType().isEnum())               { field.set(t, field.getType().getEnumConstants()[NumberUtils.INTEGER_ZERO]); }
		}
		return t;
	}


//	@SneakyThrows
//	private static void recursive(Object source, MultiValueMap<String, String> parameter, String depth) {
//
//		if(Objects.nonNull(source)) {
//			List<Field> fieldsOfClass      = Arrays.asList(Optional.ofNullable(source.getClass().getDeclaredFields()).orElse                (new Field[]{}));
//			List<Field> fieldsOfSuperClass = Arrays.asList(Optional.ofNullable(source.getClass().getSuperclass().getDeclaredFields()).orElse(new Field[]{}));
//
//			for (Field field : Stream.of(fieldsOfClass, fieldsOfSuperClass)
//					.flatMap(f -> f.stream())
//					.peek   (ReflectionUtils::makeAccessible)
//					.collect(Collectors.toList())) {
//
//				Object object    = field.get(source);
//				String fieldName = Optional.ofNullable(field.getAnnotation(com.fasterxml.jackson.annotation.JsonProperty.class))
//						                   .map       (jsonProperty -> jsonProperty.value())
//						                   .orElse    (field.getName());
//				if(Objects.nonNull(object)) {
//
//					if(defaultType.test(field))     { parameter.add(depthFormatter(depth, fieldName), defaultFormatter(object, field)); continue; }
//					if(object.getClass().isArray()) { parameter.add(depthFormatter(depth, fieldName), arrayFormatter  (object, field)); continue; }
//
//					if(List.class.equals(field.getType())) {
//
//						List fieldsOfList = List.class.cast(object);
//						for (Object fieldOfList : fieldsOfList) {
//							recursive(fieldOfList, parameter, depthFormatter(depth, String.format("%s[%d]", fieldName, fieldsOfList.indexOf(fieldOfList))));
//						}
//						continue;
//					}
//
//					recursive(field.get(source), parameter, depthFormatter(depth, field.getName()));
//				}
//			}
//		}
//	}
//
//	private static final Predicate<Field> defaultType = field -> {
//		if(field.getType().isEnum()) { return Boolean.TRUE; }
//		return ImmutableSet.of(Long.class, Integer.class, BigDecimal.class, Boolean.class, String.class, LocalTime.class, LocalDate.class, LocalDateTime.class)
//				.contains(field.getType());
//	};
//
//	private static String depthFormatter(String path, String name) {
//		return StringUtils.isBlank(path) ? name : String.format("%s.%s", path, name);
//	}
//
//	private static String defaultFormatter(Object object, Field field) {
//		if(ImmutableSet.of(LocalTime.class, LocalDate.class, LocalDateTime.class).contains(field.getType())) {
//			return dateFormatter(object, field.getAnnotation(DateTimeFormat.class));
//		}
//		return String.valueOf(object);
//	}
//
//	private static String arrayFormatter(Object object, Field field) {
//		if(object.getClass().isArray()) {
//			if(ImmutableSet.of(LocalTime[].class, LocalDate[].class, LocalDateTime[].class).contains(field.getType())) {
//				return Arrays.stream ((Object[])object)
//						.map    (target -> dateFormatter(target, field.getAnnotation(DateTimeFormat.class)))
//						.collect(Collectors.joining(Constant.String.COMMA));
//			}
//			return Arrays.stream ((Object[])object)
//					.map    (String::valueOf)
//					.collect(Collectors.joining(Constant.String.COMMA));
//		}
//		return String.valueOf(object);
//	}
//
//	private static String dateFormatter(Object object, DateTimeFormat format) {
//		if(Objects.isNull(format)) {
//			return String.valueOf(object);
//		}
//		if(LocalTime.class     == object.getClass()) return datePattern((LocalTime)    object, format);
//		if(LocalDate.class     == object.getClass()) return datePattern((LocalDate)    object, format);
//		if(LocalDateTime.class == object.getClass()) return datePattern((LocalDateTime)object, format);
//		return String.valueOf(object);
//	}
//
//	private static String datePattern(LocalDateTime date, DateTimeFormat format) {
//		if(StringUtils.isNotBlank(format.pattern())) {
//			return date.format(DateTimeFormatter.ofPattern(format.pattern()));
//		}
//		switch (format.iso()) {
//			case TIME      : return date.format(DateTimeFormatter.ISO_TIME);
//			case DATE      : return date.format(DateTimeFormatter.ISO_DATE);
//			case DATE_TIME : return date.format(DateTimeFormatter.ISO_DATE_TIME);
//			case NONE      :
//			default        : return String.valueOf(date);
//		}
//	}
//
//	private static String datePattern(LocalTime date, DateTimeFormat format) {
//		if(StringUtils.isNotBlank(format.pattern())) {
//			return date.format(DateTimeFormatter.ofPattern(format.pattern()));
//		}
//		switch (format.iso()) {
//			case TIME      : return date.format(DateTimeFormatter.ISO_TIME);
//			case NONE      :
//			default        : return String.valueOf(date);
//		}
//	}
//
//	private static String datePattern(LocalDate date, DateTimeFormat format) {
//		if(StringUtils.isNotBlank(format.pattern())) {
//			return date.format(DateTimeFormatter.ofPattern(format.pattern()));
//		}
//		switch (format.iso()) {
//			case DATE      : return date.format(DateTimeFormatter.ISO_DATE);
//			case NONE      :
//			default        : return String.valueOf(date);
//		}
//	}

	private static ObjectMapper objectMapper = null;
}
