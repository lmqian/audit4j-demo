package hello;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.audit4j.core.AnnotationTransformer;
import org.audit4j.core.ObjectSerializer;
import org.audit4j.core.ObjectToFieldsSerializer;
import org.audit4j.core.annotation.Audit;
import org.audit4j.core.annotation.AuditField;
import org.audit4j.core.annotation.DeIdentify;
import org.audit4j.core.annotation.IgnoreAudit;
import org.audit4j.core.dto.AnnotationAuditEvent;
import org.audit4j.core.dto.AuditEvent;
import org.audit4j.core.dto.Field;

/**
 * @author lmqian
 * @date 2019年4月15日 下午10:15:09
 */
public class CustomizedAnnotationTransformer implements AnnotationTransformer<AuditEvent> {

    private final static String ACTION = "action";

    private ObjectSerializer serializer;

    public CustomizedAnnotationTransformer() {
        this.serializer = new ObjectToFieldsSerializer();
    }

    public CustomizedAnnotationTransformer(ObjectSerializer objectSerializer) {
        this.serializer = objectSerializer;
    }

    @Override
    public AuditEvent transformToEvent(AnnotationAuditEvent annotationEvent) {
        AuditEvent event = null;

        if (annotationEvent.getClazz().isAnnotationPresent(Audit.class)
                && !annotationEvent.getMethod().isAnnotationPresent(IgnoreAudit.class)) {
            event = new AuditEvent();
            Audit audit = annotationEvent.getClazz().getAnnotation(Audit.class);

            // Extract fields
            event.setFields(getFields(annotationEvent.getMethod(), annotationEvent.getArgs()));

            // Extract Actor
            String annotationAction = audit.action();
            if (ACTION.equals(annotationAction)) {
                event.setAction(annotationEvent.getMethod().getDeclaringClass() + "." + annotationEvent.getMethod().getName());
            } else {
                event.setAction(annotationAction);
            }

            // Extract repository
            event.setRepository(audit.repository());

            event.setActor(annotationEvent.getActor());
            event.setOrigin(annotationEvent.getOrigin());
        } else if (!annotationEvent.getClazz().isAnnotationPresent(Audit.class)
                && annotationEvent.getMethod().isAnnotationPresent(Audit.class)) {
            event = new AuditEvent();
            Audit audit = annotationEvent.getMethod().getAnnotation(Audit.class);

            // Extract fields
            event.setFields(getFields(annotationEvent.getMethod(), annotationEvent.getArgs()));

            // Extract Actor
            String annotationAction = audit.action();
            if (ACTION.equals(annotationAction)) {
                event.setAction(annotationEvent.getMethod().getDeclaringClass() + "." + annotationEvent.getMethod().getName());
            } else {
                event.setAction(annotationAction);
            }

            // Extract repository
            event.setRepository(audit.repository());

            event.setActor(annotationEvent.getActor());
            event.setOrigin(annotationEvent.getOrigin());
        }

        return event;
    }

    /**
     * Extract fields based on annotations.
     * 
     * @param method
     *            : Class method with annotations.
     * @param params
     *            : Method parameter values.
     * 
     * @return list of fields extracted from method.
     * 
     * @since 2.4.1
     */
    private List<Field> getFields(final Method method, final Object[] params) {
        final Annotation[][] parameterAnnotations = method.getParameterAnnotations();

        final List<Field> fields = new ArrayList<Field>();

        int i = 0;
        String paramName = null;
        for (final Annotation[] annotations : parameterAnnotations) {
            final Object object = params[i++];
            boolean ignoreFlag = false;
            DeIdentify deidentify = null;
            for (final Annotation annotation : annotations) {
                if (annotation instanceof IgnoreAudit) {
                    ignoreFlag = true;
                    break;
                }
                if (annotation instanceof AuditField) {
                    final AuditField field = (AuditField) annotation;
                    paramName = field.field();
                }
                if (annotation instanceof DeIdentify) {
                    deidentify = (DeIdentify) annotation;
                }
            }

            if (ignoreFlag) {

            } else {
                if (null == paramName) {
                    paramName = "arg" + i;
                }
                serializer.serialize(fields, object, paramName, deidentify);
            }

            paramName = null;
        }
        return fields;
    }

    public void setSerializer(ObjectSerializer serializer) {
        this.serializer = serializer;
    }
}
