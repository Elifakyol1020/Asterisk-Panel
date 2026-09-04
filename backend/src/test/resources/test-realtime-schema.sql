-- H2-only projection tables. Production ps_* tables must be installed from the
-- exact Asterisk release used by the external PBX.
CREATE TABLE ps_aors (
    id VARCHAR(255) PRIMARY KEY, contact VARCHAR(255), max_contacts INTEGER,
    remove_existing VARCHAR(10), qualify_frequency INTEGER
);
CREATE TABLE ps_auths (
    id VARCHAR(255) PRIMARY KEY, auth_type VARCHAR(40), username VARCHAR(40), password VARCHAR(80)
);
CREATE TABLE ps_endpoints (
    id VARCHAR(255) PRIMARY KEY, transport VARCHAR(40), aors VARCHAR(2048), auth VARCHAR(255),
    outbound_auth VARCHAR(255), context VARCHAR(40), disallow VARCHAR(200), allow VARCHAR(200),
    direct_media VARCHAR(10), force_rport VARCHAR(10), rewrite_contact VARCHAR(10),
    rtp_symmetric VARCHAR(10), from_user VARCHAR(40), from_domain VARCHAR(40),
    callerid VARCHAR(40), set_var VARCHAR(255)
);
CREATE TABLE ps_endpoint_id_ips (
    id VARCHAR(255) PRIMARY KEY, endpoint VARCHAR(255), match VARCHAR(80)
);
CREATE TABLE queues (
    name VARCHAR(128) PRIMARY KEY, musiconhold VARCHAR(128), context VARCHAR(128), timeout INTEGER,
    ringinuse VARCHAR(10), monitor_format VARCHAR(8), announce_frequency INTEGER,
    announce_position VARCHAR(128), periodic_announce_frequency INTEGER, retry INTEGER,
    wrapuptime INTEGER, autofill VARCHAR(10), maxlen INTEGER, strategy VARCHAR(40),
    joinempty VARCHAR(128), leavewhenempty VARCHAR(128)
);
CREATE TABLE queue_members (
    queue_name VARCHAR(80) NOT NULL, interface VARCHAR(80) NOT NULL, membername VARCHAR(80),
    state_interface VARCHAR(80), penalty INTEGER, paused INTEGER, uniqueid INTEGER NOT NULL UNIQUE,
    wrapuptime INTEGER, ringinuse VARCHAR(10), PRIMARY KEY (queue_name, interface)
);
