package dev.echo.standalone.runtime.client;

final class EchoClientWorldTemplates {
    private static final EchoClientContentProfiles.Profile DEFAULT_PROFILE =
            EchoClientContentProfiles.ashfallCrashSite();
    private static final EchoClientWorldTemplate DEFAULT_TEMPLATE =
            DEFAULT_PROFILE.toWorldTemplate(EchoClientAshfallSessionFactory.instance());

    private EchoClientWorldTemplates() {
    }

    static EchoClientWorldTemplate defaultTemplate() {
        return DEFAULT_TEMPLATE;
    }

    static EchoClientWorldTemplate ashfallCrashSite() {
        return DEFAULT_TEMPLATE;
    }

    static EchoClientWorldTemplate openlandsFirstHour() {
        return EchoClientContentProfiles.openlandsFirstHour()
                .toWorldTemplate(EchoClientOpenlandsSessionFactory.instance());
    }
}
