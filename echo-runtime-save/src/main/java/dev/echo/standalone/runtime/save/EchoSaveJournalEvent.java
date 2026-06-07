package dev.echo.standalone.runtime.save;

public enum EchoSaveJournalEvent {
    BEGIN,
    STAGED,
    BACKUP_CREATED,
    BACKUP_RESTORED,
    COMMITTED,
    ROLLED_BACK,
    MIGRATION_PLANNED,
    CORRUPTION_CHECKED,
    MOD_SET_CHECKED
}
