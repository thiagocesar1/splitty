ALTER TABLE group_members
ADD COLUMN role VARCHAR(10) NOT NULL DEFAULT 'MEMBER',
ADD COLUMN joined_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP;

-- Add check constraint to ensure role is either OWNER or MEMBER
ALTER TABLE group_members
ADD CONSTRAINT chk_group_member_role CHECK (role IN ('OWNER', 'MEMBER'));
