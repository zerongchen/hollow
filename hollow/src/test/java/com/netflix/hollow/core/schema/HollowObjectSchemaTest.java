/*
 *  Copyright 2016-2019 Netflix, Inc.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 *
 */
package com.netflix.hollow.core.schema;

import com.netflix.hollow.api.error.IncompatibleSchemaException;
import com.netflix.hollow.core.read.filter.HollowFilterConfig;
import com.netflix.hollow.core.schema.HollowObjectSchema.FieldType;
import static com.netflix.hollow.test.AssertShim.*;
import org.junit.jupiter.api.Test;

public class HollowObjectSchemaTest {
    @Test
    public void findsCommonSchemas() {
        HollowObjectSchema s1 = new HollowObjectSchema("Test", 2, "F2");
        s1.addField("F1", FieldType.INT);
        s1.addField("F2", FieldType.LONG);

        HollowObjectSchema s2 = new HollowObjectSchema("Test", 2, "F2");
        s2.addField("F2", FieldType.LONG);
        s2.addField("F3", FieldType.STRING);

        HollowObjectSchema commonSchema = s1.findCommonSchema(s2);

        assertEquals(1, commonSchema.numFields());
        assertEquals("F2", commonSchema.getFieldName(0));
        assertEquals(FieldType.LONG, commonSchema.getFieldType(0));

        assertEquals(s1.getPrimaryKey(), s2.getPrimaryKey());
        assertEquals(s1.getPrimaryKey(), commonSchema.getPrimaryKey());

        {
            HollowObjectSchema s3 = new HollowObjectSchema("Test", 2, "F3");
            s3.addField("F2", FieldType.LONG);
            s3.addField("F3", FieldType.STRING);

            HollowObjectSchema c3 = s1.findCommonSchema(s3);
            assertNotEquals(s1.getPrimaryKey(), s3.getPrimaryKey());
            assertNotEquals(s1.getPrimaryKey(), c3.getPrimaryKey());
            assertNull(c3.getPrimaryKey());
        }
    }

    @Test
    public void findCommonSchema_incompatible() {
        try {
            HollowObjectSchema s1 = new HollowObjectSchema("Test", 2, "F1");
            s1.addField("F1", FieldType.INT);
            HollowObjectSchema s2 = new HollowObjectSchema("Test", 2, "F1");
            s2.addField("F1", FieldType.STRING);
            s1.findCommonSchema(s2);
            fail("Expected IncompatibleSchemaException");
        } catch (IncompatibleSchemaException e) {
            assertEquals("Test", e.getTypeName());
            assertEquals("F1", e.getFieldName());
        }
    }

    @Test
    public void findsUnionSchemas() {
        HollowObjectSchema s1 = new HollowObjectSchema("Test", 2, "F2");
        s1.addField("F1", FieldType.INT);
        s1.addField("F2", FieldType.LONG);

        HollowObjectSchema s2 = new HollowObjectSchema("Test", 2, "F2");
        s2.addField("F2", FieldType.LONG);
        s2.addField("F3", FieldType.STRING);

        HollowObjectSchema unionSchema = s1.findUnionSchema(s2);

        assertEquals(3, unionSchema.numFields());
        assertEquals("F1", unionSchema.getFieldName(0));
        assertEquals(FieldType.INT, unionSchema.getFieldType(0));
        assertEquals("F2", unionSchema.getFieldName(1));
        assertEquals(FieldType.LONG, unionSchema.getFieldType(1));
        assertEquals("F3", unionSchema.getFieldName(2));
        assertEquals(FieldType.STRING, unionSchema.getFieldType(2));

        assertEquals(s1.getPrimaryKey(), s2.getPrimaryKey());
        assertEquals(s1.getPrimaryKey(), unionSchema.getPrimaryKey());

        {
            HollowObjectSchema s3 = new HollowObjectSchema("Test", 2, "F3");
            s3.addField("F2", FieldType.LONG);
            s3.addField("F3", FieldType.STRING);

            HollowObjectSchema u3 = s1.findUnionSchema(s3);
            assertNotEquals(s1.getPrimaryKey(), u3.getPrimaryKey());
            assertNotEquals(s1.getPrimaryKey(), u3.getPrimaryKey());
            assertNull(u3.getPrimaryKey());
        }
    }

    @Test
    public void filterSchema() {
        HollowObjectSchema s1 = new HollowObjectSchema("Test", 2, "F2");
        s1.addField("F1", FieldType.INT);
        s1.addField("F2", FieldType.LONG);
        assertEquals(2, s1.numFields());

        HollowFilterConfig filter = new HollowFilterConfig();
        filter.addField("Test", "F2");
        HollowObjectSchema s2 = s1.filterSchema(filter);
        assertEquals(1, s2.numFields());
        assertEquals("F2", s2.getFieldName(0));

        assertEquals(s1.getPrimaryKey(), s2.getPrimaryKey());
    }

    @Test
    public void testEquals() {
        {
            HollowObjectSchema s1 = new HollowObjectSchema("Test", 2);
            s1.addField("F1", FieldType.INT);
            s1.addField("F2", FieldType.LONG);

            HollowObjectSchema s2 = new HollowObjectSchema("Test", 2);
            s2.addField("F1", FieldType.INT);
            s2.addField("F2", FieldType.LONG);

            assertEquals(s1, s2);
        }

        {
            HollowObjectSchema s1 = new HollowObjectSchema("Test", 2);
            s1.addField("F1", FieldType.INT);
            s1.addField("F2", FieldType.LONG);

            HollowObjectSchema s2 = new HollowObjectSchema("Test", 1);
            s2.addField("F1", FieldType.INT);

            assertNotEquals(s1, s2);
        }

    }

    @Test
    public void testEqualsWithPrimaryKey() {
        {
            HollowObjectSchema s1 = new HollowObjectSchema("Test", 2, "F2");
            s1.addField("F1", FieldType.INT);
            s1.addField("F2", FieldType.LONG);

            HollowObjectSchema s2 = new HollowObjectSchema("Test", 2, "F2");
            s2.addField("F1", FieldType.INT);
            s2.addField("F2", FieldType.LONG);

            assertEquals(s1, s2);
            assertEquals(s1.getPrimaryKey(), s2.getPrimaryKey());
        }

        {
            HollowObjectSchema s1 = new HollowObjectSchema("Test", 2, "F1", "F2");
            s1.addField("F1", FieldType.INT);
            s1.addField("F2", FieldType.LONG);

            HollowObjectSchema s2 = new HollowObjectSchema("Test", 2, "F1", "F2");
            s2.addField("F1", FieldType.INT);
            s2.addField("F2", FieldType.LONG);

            assertEquals(s1, s2);
            assertEquals(s1.getPrimaryKey(), s2.getPrimaryKey());
        }

        {
            HollowObjectSchema s1 = new HollowObjectSchema("Test", 2, "F1", "F2");
            s1.addField("F1", FieldType.INT);
            s1.addField("F2", FieldType.LONG);

            HollowObjectSchema s2 = new HollowObjectSchema("Test", 2, "F1");
            s2.addField("F1", FieldType.INT);
            s2.addField("F2", FieldType.LONG);

            assertNotEquals(s1, s2);
            assertNotEquals(s1.getPrimaryKey(), s2.getPrimaryKey());
        }

        {
            HollowObjectSchema s1 = new HollowObjectSchema("Test", 2);
            s1.addField("F1", FieldType.INT);
            s1.addField("F2", FieldType.LONG);

            HollowObjectSchema s2 = new HollowObjectSchema("Test", 2, "F1");
            s2.addField("F1", FieldType.INT);
            s2.addField("F2", FieldType.LONG);

            assertNotEquals(s1, s2);
            assertNotEquals(s1.getPrimaryKey(), s2.getPrimaryKey());
        }

    }
}
