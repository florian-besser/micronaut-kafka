/*
 * Copyright 2017-2019 original authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.micronaut.configuration.kafka.graal;

import com.oracle.svm.core.annotate.Alias;
import com.oracle.svm.core.annotate.RecomputeFieldValue;
import com.oracle.svm.core.annotate.Substitute;
import com.oracle.svm.core.annotate.TargetClass;
import org.apache.kafka.common.metrics.KafkaMetric;
import org.apache.kafka.common.metrics.Metrics;
import org.apache.kafka.common.metrics.MetricsReporter;
import org.apache.kafka.common.record.CompressionType;
import org.apache.kafka.common.utils.AppInfoParser;

import java.util.List;
import java.util.Map;
import java.util.zip.CRC32;
import java.util.zip.Checksum;


//CHECKSTYLE:OFF
@TargetClass(className = "org.apache.kafka.common.utils.Crc32C$Java9ChecksumFactory")
@Substitute
final class Java9ChecksumFactory {

    @Substitute
    public Checksum create() {
        return new CRC32();
    }

}

// Replace unsupported compression types
@TargetClass(className = "org.apache.kafka.common.record.CompressionType")
final class CompressionTypeSubs {

    // @Alias @RecomputeFieldValue(kind = RecomputeFieldValue.Kind.FromAlias)
    // public static CompressionType LZ4 = CompressionType.GZIP;

    @Alias @RecomputeFieldValue(kind = RecomputeFieldValue.Kind.FromAlias)
    public static CompressionType SNAPPY = CompressionType.GZIP;

    @Alias @RecomputeFieldValue(kind = RecomputeFieldValue.Kind.FromAlias)
    public static CompressionType ZSTD = CompressionType.GZIP;
}

// Replace JMX metrics, no operable on GraalVM
@TargetClass(className = "org.apache.kafka.common.metrics.JmxReporter")
@Substitute
final class NoopReporter implements MetricsReporter {

    @Substitute
    public NoopReporter() {
    }

    @Substitute
    public NoopReporter(String prefix) {
    }

    @Override
    @Substitute
    public void init(List<KafkaMetric> metrics) {
    }

    @Override
    @Substitute
    public void metricChange(KafkaMetric metric) {
    }

    @Override
    @Substitute
    public void metricRemoval(KafkaMetric metric) {
    }

    @Override
    @Substitute
    public void close() {
    }

    @Override
    @Substitute
    public void configure(Map<String, ?> configs) {
    }
}

@TargetClass(AppInfoParser.class)
final class AppInfoParserNoJMX {

    @Substitute
    public static void registerAppInfo(String prefix, String id, Metrics metrics, long nowMs) {
       // no-op
    }

    @Substitute
    public static void unregisterAppInfo(String prefix, String id, Metrics metrics) {
        // no-op
    }
}
//CHECKSTYLE:ON