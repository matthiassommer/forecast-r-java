/*
 * Copyright (c) 2015 Matthias Sommer, All rights reserved.
 * This program and the accompanying materials are made available
 * under the terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package forecasting.plotting;

import org.jetbrains.annotations.Nullable;
import org.rosuda.REngine.Rserve.RConnection;
import org.rosuda.REngine.Rserve.RserveException;

import java.nio.file.Paths;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Plot a simple curve graph.
 */
public class PlotCurves {

    public static void plotR(String tsName, RConnection rc, String pdfPath) throws RserveException {
        plotR(tsName, pdfPath, null, null, null, rc);
    }

    public static void plotR(String tsName, String pdfPath, @Nullable String title, @Nullable String xlabel, @Nullable String ylabel, RConnection rc) throws RserveException {
        Format formatter = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
        String date = formatter.format(new Date());
        String filename = tsName + "_" + date + ".pdf";

        rc.voidEval("pdf(\"" + pdfPath + "\")");

        StringBuilder sb = new StringBuilder();
        sb.append("plot(").append(tsName);

        if (title != null) {
            sb.append(", main=\"").append(title).append("\"");
        }

        if (xlabel != null) {
            sb.append(", xlab=\"").append(xlabel).append("\"");
        }

        if (ylabel != null) {
            sb.append(", ylab=\"").append(ylabel).append("\"");
        }

        sb.append(")");

        rc.voidEval(sb.toString());
        rc.voidEval("dev.off()");
    }
}
