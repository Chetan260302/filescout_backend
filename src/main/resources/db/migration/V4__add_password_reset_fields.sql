-- Add password reset token fields to users table
ALTER TABLE `users` ADD COLUMN `reset_token` VARCHAR(255) DEFAULT NULL;
ALTER TABLE `users` ADD COLUMN `reset_token_expiry` DATETIME(6) DEFAULT NULL;
