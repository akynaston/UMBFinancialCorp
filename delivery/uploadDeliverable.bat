set home=/cygdrive/c/users/trivir
wsl rsync  -e "ssh" -tvP --perms --chmod o+r * --no-compress --checksum --inplace akynaston@ftp.trivir.com:/srv/ftp/outgoing/umbbank/
pause

