# 깃랩 백업

## 백업 설정

깃랩 데이터 백업은 데이터베이스가 포함 된 아카이브 파일을 생성하고, 모든 레파지토리 및 모든 첨부 파일, Artifacts 를 포함합니다.

백업은 정확히 동일한 버전 및 유형 (CE / EE)으로 만 복원 할 수 있습니다. 동일한 버젼끼리의 깃랩 서버간에는 백업, 리스토어 수행이 자유롭습니다.


### rsync 설치

다음 명령어로 rsync 를 설치합니다.

```
$ sudo yum install rsync -y
```

### 백업 타임스탬프
 
백업 아카이브 파일은 `config/gitlab.yml` 에 지정된 backup_path 에 저장됩니다.

파일 이름은 `[TIMESTAMP]_gitlab_backup.tar` 이며, 여기서 TIMESTAMP 로 백업이 생성 된 시간과 GitLab 버전을 식별합니다.

예를 들어 백업 이름이 `1493107454_2017_04_25_9.1.0_gitlab_backup.tar` 인 경우, 타임스탬프는 `1493107454_2017_04_25_9.1.0` 입니다.

### 백업 만들기

백업을 수행하기 위해 깃랩 서버를 내릴 필요는 없습니다. 다음 명령어로 백업을 생성합니다.

```
$ sudo gitlab-rake gitlab:backup:create

Dumping database tables:
- Dumping table events... [DONE]
- Dumping table issues... [DONE]
- Dumping table keys... [DONE]
- Dumping table merge_requests... [DONE]
- Dumping table milestones... [DONE]
- Dumping table namespaces... [DONE]
- Dumping table notes... [DONE]
- Dumping table projects... [DONE]
- Dumping table protected_branches... [DONE]
- Dumping table schema_migrations... [DONE]
- Dumping table services... [DONE]
- Dumping table snippets... [DONE]
- Dumping table taggings... [DONE]
- Dumping table tags... [DONE]
- Dumping table users... [DONE]
- Dumping table users_projects... [DONE]
- Dumping table web_hooks... [DONE]
- Dumping table wikis... [DONE]
Dumping repositories:
- Dumping repository abcd... [DONE]
Creating backup archive: $TIMESTAMP_gitlab_backup.tar [DONE]
Deleting tmp directories...[DONE]
Deleting old backups... [SKIPPING]
```

### Uploading backups to a remote (cloud) storage

GitLab 7.4부터 백업 스크립트가 생성 한 `.tar` 파일을 업로드하도록 할 수 있도록 업데이트 되었습니다.

아래 예제에서는 Amazon S3 에 저장되도록 설정하지만, 다음과 같은 백업 저장소를 선택할 수 있습니다.

- [imports cloud drivers](https://gitlab.com/gitlab-org/gitlab-ce/blob/30f5b9a5b711b46f1065baf755e413ceced5646b/Gemfile#L88)
for AWS, Google, OpenStack Swift, Rackspace and Aliyun as well. 

- A local driver is [also available](#uploading-to-locally-mounted-shares).

### Using Amazon S3

옴니버스 패키지 설정:

1. `/etc/gitlab/gitlab.rb` 에 다음과 같이 추가합니다. :

    ```ruby
    gitlab_rails['backup_upload_connection'] = {
      'provider' => 'AWS',
      'region' => 'eu-west-1',
      'aws_access_key_id' => 'AKIAKIAKI',
      'aws_secret_access_key' => 'secret123'
      # If using an IAM Profile, don't configure aws_access_key_id & aws_secret_access_key
      # 'use_iam_profile' => true
    }
    gitlab_rails['backup_upload_remote_directory'] = 'my.s3.bucket'
    ```

2. 바뀐 설정을 적용합니다.

```
$ sudo gitlab-ctl reconfigure
```

### uploading-to-locally-mounted-shares

백업저장소로 (`NFS` / `CIFS` / `SMB` / etc.) 등과 같은 마운트 디스크를 통해 수행할 수 있는데, 
이때는 Fog [`Local`](https://github.com/fog/fog-local#usage) 스토리지 프로바이더를 사용하게 됩니다.

The directory pointed to by the `local_root` key **must** be owned by the `git`
user **when mounted** (mounting with the `uid=` of the `git` user for `CIFS` and
`SMB`) or the user that you are executing the backup tasks under (for omnibus
packages, this is the `git` user).


The `backup_upload_remote_directory` **must** be set in addition to the
`local_root` key. This is the sub directory inside the mounted directory that
backups will be copied to, and will be created if it does not exist. If the
directory that you want to copy the tarballs to is the root of your mounted
directory, just use `.` instead.

옴니버스 패키지 설정: (설정 후 `$ sudo gitlab-ctl reconfigure` 를 수행하세요.)

```ruby
gitlab_rails['backup_upload_connection'] = {
  :provider => 'Local',
  :local_root => '/mnt/backups'
}

# The directory inside the mounted folder to copy backups to
# Use '.' to store them in the root directory
gitlab_rails['backup_upload_remote_directory'] = 'gitlab_backups'
```

### Specifying a custom directory for backups

If you want to group your backups you can pass a `DIRECTORY` environment variable:

```
sudo gitlab-rake gitlab:backup:create DIRECTORY=daily
sudo gitlab-rake gitlab:backup:create DIRECTORY=weekly
```

### Backup archive permissions

The backup archives created by GitLab (`1393513186_2014_02_27_gitlab_backup.tar`)
will have owner/group git:git and 0600 permissions by default.
This is meant to avoid other system users reading GitLab's data.
If you need the backup archives to have different permissions you can use the 'archive_permissions' setting.

```
# In /etc/gitlab/gitlab.rb, for omnibus packages
gitlab_rails['backup_archive_permissions'] = 0644 # Makes the backup archives world-readable
```


### Storing configuration files

Please be informed that a backup does not store your configuration
files. One reason for this is that your database contains encrypted
information for two-factor authentication. Storing encrypted
information along with its key in the same place defeats the purpose
of using encryption in the first place!

If you use an Omnibus package please see the [instructions in the readme to backup your configuration](https://gitlab.com/gitlab-org/omnibus-gitlab/blob/master/README.md#backup-and-restore-omnibus-gitlab-configuration).
If you have a cookbook installation there should be a copy of your configuration in Chef.
If you installed from source, please consider backing up your `config/secrets.yml` file, `gitlab.yml` file, any SSL keys and certificates, and your [SSH host keys](https://superuser.com/questions/532040/copy-ssh-keys-from-one-server-to-another-server/532079#532079).

At the very **minimum** you should backup `/etc/gitlab/gitlab.rb` and
`/etc/gitlab/gitlab-secrets.json` (Omnibus), or
`/home/git/gitlab/config/secrets.yml` (source) to preserve your database
encryption key.

### Configuring cron to make daily backups

>**Note:**
The following cron jobs do not [backup your GitLab configuration files](#storing-configuration-files)
or [SSH host keys](https://superuser.com/questions/532040/copy-ssh-keys-from-one-server-to-another-server/532079#532079).

**For Omnibus installations**

To schedule a cron job that backs up your repositories and GitLab metadata, use the root user:

```
sudo su -
crontab -e
```

There, add the following line to schedule the backup for everyday at 2 AM:

```
0 2 * * * /opt/gitlab/bin/gitlab-rake gitlab:backup:create CRON=1
```

You may also want to set a limited lifetime for backups to prevent regular
backups using all your disk space.  To do this add the following lines to
`/etc/gitlab/gitlab.rb` and reconfigure:

```
# limit backup lifetime to 7 days - 604800 seconds
gitlab_rails['backup_keep_time'] = 604800
```

Note that the `backup_keep_time` configuration option only manages local
files. GitLab does not automatically prune old files stored in a third-party
object storage (e.g., AWS S3) because the user may not have permission to list
and delete files. We recommend that you configure the appropriate retention
policy for your object storage. For example, you can configure [the S3 backup
policy as described here](http://stackoverflow.com/questions/37553070/gitlab-omnibus-delete-backup-from-amazon-s3).

## 백업 리스토어

GitLab provides a simple command line interface to restore your whole installation,
and is flexible enough to fit your needs.

The [restore prerequisites section](#restore-prerequisites) includes crucial
information. Make sure to read and test the whole restore process at least once
before attempting to perform it in a production environment.

You can only restore a backup to **exactly the same version and type (CE/EE)** of
GitLab that you created it on, for example CE 9.1.0.

### Restore prerequisites

You need to have a working GitLab installation before you can perform
a restore. This is mainly because the system user performing the
restore actions ('git') is usually not allowed to create or delete
the SQL database it needs to import data into ('gitlabhq_production').
All existing data will be either erased (SQL) or moved to a separate
directory (repositories, uploads).

To restore a backup, you will also need to restore `/etc/gitlab/gitlab-secrets.json`
(for Omnibus packages) or `/home/git/gitlab/.secret` (for installations
from source). 
If you fail to restore this encryption key file along with the application data
backup, users with two-factor authentication enabled and GitLab Runners will
lose access to your GitLab server.

Depending on your case, you might want to run the restore command with one or
more of the following options:

- `BACKUP=timestamp_of_backup` - Required if more than one backup exists.
  Read what the [backup timestamp is about](#backup-timestamp).
- `force=yes` - Do not ask if the authorized_keys file should get regenerated.

### Restore for Omnibus installations

This procedure assumes that:

- You have installed the **exact same version and type (CE/EE)** of GitLab
  Omnibus with which the backup was created.
- You have run `sudo gitlab-ctl reconfigure` at least once.
- GitLab is running.  If not, start it using `sudo gitlab-ctl start`.

First make sure your backup tar file is in the backup directory described in the
`gitlab.rb` configuration `gitlab_rails['backup_path']`. The default is
`/var/opt/gitlab/backups`.

```shell
sudo cp 1493107454_2017_04_25_9.1.0_gitlab_backup.tar /var/opt/gitlab/backups/
```

Stop the processes that are connected to the database.  Leave the rest of GitLab
running:

```shell
sudo gitlab-ctl stop unicorn
sudo gitlab-ctl stop sidekiq
# Verify
sudo gitlab-ctl status
```

Next, restore the backup, specifying the timestamp of the backup you wish to
restore:

```shell
# This command will overwrite the contents of your GitLab database!
sudo gitlab-rake gitlab:backup:restore BACKUP=1493107454_2017_04_25_9.1.0
```

Next, restore `/etc/gitlab/gitlab-secrets.json` if necessary as mentioned above.

Restart and check GitLab:

```shell
sudo gitlab-ctl restart
sudo gitlab-rake gitlab:check SANITIZE=true
```

If there is a GitLab version mismatch between your backup tar file and the installed
version of GitLab, the restore command will abort with an error. Install the
[correct GitLab version](https://packages.gitlab.com/gitlab/) and try again.


## Alternative backup strategies

If your GitLab server contains a lot of Git repository data you may find the GitLab backup script to be too slow.
In this case you can consider using filesystem snapshots as part of your backup strategy.

Example: Amazon EBS

> A GitLab server using omnibus-gitlab hosted on Amazon AWS.
> An EBS drive containing an ext4 filesystem is mounted at `/var/opt/gitlab`.
> In this case you could make an application backup by taking an EBS snapshot.
> The backup includes all repositories, uploads and Postgres data.

Example: LVM snapshots + rsync

> A GitLab server using omnibus-gitlab, with an LVM logical volume mounted at `/var/opt/gitlab`.
> Replicating the `/var/opt/gitlab` directory using rsync would not be reliable because too many files would change while rsync is running.
> Instead of rsync-ing `/var/opt/gitlab`, we create a temporary LVM snapshot, which we mount as a read-only filesystem at `/mnt/gitlab_backup`.
> Now we can have a longer running rsync job which will create a consistent replica on the remote server.
> The replica includes all repositories, uploads and Postgres data.

If you are running GitLab on a virtualized server you can possibly also create VM snapshots of the entire GitLab server.
It is not uncommon however for a VM snapshot to require you to power down the server, so this approach is probably of limited practical use.


## Troubleshooting

### Restoring database backup using omnibus packages outputs warnings

If you are using backup restore procedures you might encounter the following warnings:

```
psql:/var/opt/gitlab/backups/db/database.sql:22: ERROR:  must be owner of extension plpgsql
psql:/var/opt/gitlab/backups/db/database.sql:2931: WARNING:  no privileges could be revoked for "public" (two occurrences)
psql:/var/opt/gitlab/backups/db/database.sql:2933: WARNING:  no privileges were granted for "public" (two occurrences)
```

Be advised that, backup is successfully restored in spite of these warnings.

The rake task runs this as the `gitlab` user which does not have the superuser access to the database. When restore is initiated it will also run as `gitlab` user but it will also try to alter the objects it does not have access to.
Those objects have no influence on the database backup/restore but they give this annoying warning.

For more information see similar questions on postgresql issue tracker[here](http://www.postgresql.org/message-id/201110220712.30886.adrian.klaver@gmail.com) and [here](http://www.postgresql.org/message-id/2039.1177339749@sss.pgh.pa.us) as well as [stack overflow](http://stackoverflow.com/questions/4368789/error-must-be-owner-of-language-plpgsql).



# 깃랩 환경설정 백업

## Backup and restore Omnibus GitLab configuration

It is recommended to keep a copy of `/etc/gitlab`, or at least of
`/etc/gitlab/gitlab-secrets.json`, in a safe place. If you ever
need to restore a GitLab application backup you need to also restore
`gitlab-secrets.json`. If you do not, GitLab users who are using
two-factor authentication will lose access to your GitLab server
and 'secure variables' stored in GitLab CI will be lost.

It is not recommended to store your configuration backup in the
same place as your application data backup, see below.

All configuration for omnibus-gitlab is stored in `/etc/gitlab`. To backup your
configuration, just backup this directory.

```shell
# Example backup command for /etc/gitlab:
# Create a time-stamped .tar file in the current directory.
# The .tar file will be readable only to root.
sudo sh -c 'umask 0077; tar -cf $(date "+etc-gitlab-%s.tar") -C / etc/gitlab'
```

To create a daily application backup, edit the cron table for user root:

```shell
sudo crontab -e -u root
```

The cron table will appear in an editor.

Enter the command to create a compressed tar file containing the contents of
`/etc/gitlab/`.  For example, schedule the backup to run every morning after a
weekday, Tuesday (day 2) through Saturday (day 6):

```
15 04 * * 2-6  umask 0077; tar cfz /secret/gitlab/backups/$(date "+etc-gitlab-\%s.tgz") -C / etc/gitlab

```

[cron is rather particular](http://www.pantz.org/software/cron/croninfo.html)
about the cron table. Note:

- The empty line after the command
- The escaped percent character:  \%

You can extract the .tar file as follows.

```shell
# Rename the existing /etc/gitlab, if any
sudo mv /etc/gitlab /etc/gitlab.$(date +%s)
# Change the example timestamp below for your configuration backup
sudo tar -xf etc-gitlab-1399948539.tar -C /
```

Remember to run `sudo gitlab-ctl reconfigure` after restoring a configuration
backup.

NOTE: Your machines SSH host keys are stored in a separate location at `/etc/ssh/`. Be sure to also [backup and restore those keys](https://superuser.com/questions/532040/copy-ssh-keys-from-one-server-to-another-server/532079#532079) to avoid man-in-the-middle attack warnings if you have to perform a full machine restore.

### Separate configuration backups from application data

Do not store your GitLab application backups (Git repositories, SQL
data) in the same place as your configuration backup (`/etc/gitlab`).
The `gitlab-secrets.json` file (and possibly also the `gitlab.rb`
file) contain database encryption keys to protect sensitive data
in the SQL database:

- GitLab two-factor authentication (2FA) user secrets ('QR codes')
- GitLab CI 'secure variables'

If you separate your configuration backup from your application data backup,
you reduce the chance that your encrypted application data will be
lost/leaked/stolen together with the keys needed to decrypt it.

## Creating an application backup

To create a backup of your repositories and GitLab metadata, follow the
[backup create documentation](https://docs.gitlab.com/ce/raketasks/backup_restore.html#creating-a-backup-of-the-gitlab-system).

Backup create will store a tar file in `/var/opt/gitlab/backups`.

If you want to store your GitLab backups in a different directory, add the
following setting to `/etc/gitlab/gitlab.rb` and run `sudo gitlab-ctl
reconfigure`:

```ruby
gitlab_rails['backup_path'] = '/mnt/backups'
```

## Restoring an application backup

See [backup restore documentation](https://docs.gitlab.com/ce/raketasks/backup_restore.html#restore-for-omnibus-installations).

## Upload backups to remote (cloud) storage

For details check [backup restore document of GitLab CE](https://docs.gitlab.com/ce/raketasks/backup_restore.html#uploading-backups-to-a-remote-cloud-storage).

## Manually manage backup directory

Omnibus-gitlab creates the backup directory set with `gitlab_rails['backup_path']`. The directory is owned by the user that is running GitLab and it has strict permissions set to be accessible to only that user.
That directory will hold backup archives and they contain sensitive information.
In some organizations permissions need to be different because of, for example, shipping the backup archives offsite.

To disable backup directory management, in `/etc/gitlab/gitlab.rb` set:

```ruby
gitlab_rails['manage_backup_path'] = false
```
*Warning* If you set this configuration option, it is up to you to create the directory specified in `gitlab_rails['backup_path']` and to set permissions
which will allow user specified in `user['username']` to have correct access. Failing to do so will prevent GitLab from creating the backup archive.
