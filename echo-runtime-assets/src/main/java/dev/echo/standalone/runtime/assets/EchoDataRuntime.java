package dev.echo.standalone.runtime.assets;

import java.util.List;
import java.util.Objects;

public record EchoDataRuntime(List<EchoDataPack> dataPacks) {
    public EchoDataRuntime {
        Objects.requireNonNull(dataPacks, "dataPacks");
        dataPacks = List.copyOf(dataPacks);
    }
}
