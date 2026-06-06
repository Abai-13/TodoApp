package com.todoapp.storage;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.todoapp.model.Todo;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

/**
 * 数据持久化工具类，负责 ArrayList<Todo> 与 JSON 文件的读写。
 * 所有方法均为静态方法，无需实例化。
 */
public class Storage {

    /** 数据文件相对路径 */
    private static final String DATA_FILE = "data/todos.json";

    /** Gson 实例（美化输出，便于人工查看 JSON 文件） */
    private static final Gson gson = new GsonBuilder()
            .setPrettyPrinting()
            .create();

    /** ArrayList<Todo> 的类型令牌，供 Gson 反序列化使用 */
    private static final Type TODO_LIST_TYPE =
            new TypeToken<ArrayList<Todo>>() {}.getType();

    // 禁止实例化
    private Storage() {}

    /**
     * 将 Todo 列表序列化为 JSON 并写入文件。
     *
     * @param list 待保存的 Todo 列表，不可为 null
     */
    public static void save(ArrayList<Todo> list) {
        if (list == null) {
            System.err.println("[Storage] 保存失败：列表为 null，已忽略。");
            return;
        }

        File file = new File(DATA_FILE);
        File parentDir = file.getParentFile();

        // 确保上级目录存在（如 data/）
        if (parentDir != null && !parentDir.exists()) {
            if (!parentDir.mkdirs()) {
                System.err.println("[Storage] 保存失败：无法创建目录 " + parentDir.getPath());
                return;
            }
        }

        try (Writer writer = new OutputStreamWriter(
                new FileOutputStream(file), StandardCharsets.UTF_8)) {
            gson.toJson(list, writer);
            System.out.println("[Storage] 已保存 " + list.size() + " 条记录到 " + DATA_FILE);
        } catch (IOException e) {
            System.err.println("[Storage] 保存失败：" + e.getMessage());
        }
    }

    /**
     * 从 JSON 文件读取并反序列化为 Todo 列表。
     * 若文件不存在，返回空列表；若 JSON 格式损坏，打印警告并返回空列表。
     *
     * @return 读取到的 Todo 列表，永不返回 null
     */
    public static ArrayList<Todo> load() {
        File file = new File(DATA_FILE);

        if (!file.exists()) {
            System.out.println("[Storage] 数据文件 " + DATA_FILE + " 不存在，返回空列表。");
            return new ArrayList<>();
        }

        try (Reader reader = new InputStreamReader(
                new FileInputStream(file), StandardCharsets.UTF_8)) {
            ArrayList<Todo> list = gson.fromJson(reader, TODO_LIST_TYPE);
            if (list == null) {
                System.out.println("[Storage] JSON 内容为空，返回空列表。");
                return new ArrayList<>();
            }
            System.out.println("[Storage] 已加载 " + list.size() + " 条记录。");
            return list;
        } catch (IOException e) {
            System.err.println("[Storage] 加载失败：" + e.getMessage());
            return new ArrayList<>();
        } catch (com.google.gson.JsonSyntaxException e) {
            System.err.println("[Storage] JSON 格式损坏：" + e.getMessage());
            return new ArrayList<>();
        }
    }
}
