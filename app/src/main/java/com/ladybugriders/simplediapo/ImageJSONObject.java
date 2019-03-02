package com.ladybugriders.simplediapo;

import org.json.JSONException;
import org.json.JSONObject;

public class ImageJSONObject extends JSONObject
{
    public enum PathType
    {
        Absolute,
        Relative
    }

    private static final String FILE_PATH_KEY = "file_path";
    private static final String MD5_KEY = "md5";
    private static final String PATH_TYPE_KEY = "path_type";
    private static final String ABSOLUTE_PATH_TYPE_KEY = "absolute";

    private String m_filePath;
    private String m_md5;
    private PathType m_pathType;

    public ImageJSONObject(String data) throws JSONException {
        super(data);

        m_filePath = getString((FILE_PATH_KEY));
        m_md5 = getString((MD5_KEY));
        m_pathType = getString(PATH_TYPE_KEY) == ABSOLUTE_PATH_TYPE_KEY ? PathType.Absolute : PathType.Relative;
    }

    public String GetFilePath()
    {
        return m_filePath;
    }

    public String GetMD5()
    {
        return m_md5;
    }

    public PathType GetPathType()
    {
        return m_pathType;
    }
}
