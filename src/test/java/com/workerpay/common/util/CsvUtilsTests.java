package com.workerpay.common.util;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class CsvUtilsTests {

    @Test
    void cellNeutralizesSpreadsheetFormulas() {
        assertThat(CsvUtils.cell("=SUM(1,1)")).isEqualTo("\"'=SUM(1,1)\"");
        assertThat(CsvUtils.cell("+SUM(1,1)")).isEqualTo("\"'+SUM(1,1)\"");
        assertThat(CsvUtils.cell("-10+20")).isEqualTo("'-10+20");
        assertThat(CsvUtils.cell("@HYPERLINK")).isEqualTo("'@HYPERLINK");
        assertThat(CsvUtils.cell("\t=SUM(1,1)")).isEqualTo("\"'\t=SUM(1,1)\"");
    }

    @Test
    void cellStillEscapesCsvSyntax() {
        assertThat(CsvUtils.cell("Ana, \"Lopez\"")).isEqualTo("\"Ana, \"\"Lopez\"\"\"");
    }
}
