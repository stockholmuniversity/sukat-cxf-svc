package se.su.it.svc.query

class UidNumberQuery
{
    def sukatSql

    public getUidNumber(String uid)
    {
        def row = sukatSql.firstRow("SELECT * FROM uidnumber WHERE uid = :uid", [uid: uid])

        if (row == null)
        {
            sukatSql.execute("INSERT INTO uidnumber (uid) VALUE (:uid)", [uid: uid])

            row = sukatSql.firstRow("SELECT * FROM uidnumber WHERE uid = :uid", [uid: uid])
        }

        return row.id
    }
}

