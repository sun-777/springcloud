package com.sun.server.context;

import com.sun.common.id.CustomIdGenerator;
import com.sun.common.id.IdGenerator;
import com.sun.common.util.Constants;
import com.sun.common.util.StringUtil;
import com.sun.server.context.configure.IdGeneratorProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static com.sun.common.util.Constants.HOSTS_DEFAULT;
import static com.sun.common.util.Constants.HOSTS_WINDOWS;


/**
 * 当SpringBoot容器初始化完成之后，执行一些必要的初始化
 * 
 * @author Sun Xiaodong
 *
 */
@Component
public class ConfigurationInitializer implements ApplicationListener<ContextRefreshedEvent> {

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        ApplicationContext applicationContext = event.getApplicationContext();
        
        // 获取application.yaml中自定义的配置
        final IdGeneratorProperties properties = Objects.requireNonNull(applicationContext.getBean(IdGeneratorProperties.class));

        // 配置自定义的ID生成器
        CustomIdGenerator.setIdGenerator(getIdGenerator(properties));
    }



    private static IdGenerator.Id getIdGenerator(final IdGeneratorProperties properties) {
        final IdGeneratorProperties.SnowflakeId snowflakeId = properties.getSnowflakeId();
        if (null != snowflakeId && snowflakeId.getEnable()) {
            setWorkId(snowflakeId);
            return IdGenerator.Id.SNOWFLAKEID;
        }

        final IdGeneratorProperties.ObjectId objectId = properties.getObjectId();
        if (null != objectId && objectId.getEnable()) {
            return IdGenerator.Id.OBJECTID;
        }

        // 既没配置SnowflakeId，也没配置ObjectId，则抛出异常
        throw new IllegalStateException("No id-generator configuration");
    }



    private static void setWorkId(final IdGeneratorProperties.SnowflakeId snowflakeId) {
        try {
            InetAddress inetAddr = InetAddress.getLocalHost();
            String address = inetAddr.getHostAddress();
            // 获取PC name
            String pcName = inetAddr.getHostName();
            // 最多两个元素，一个是PC name，还有一个是hosts文件中IP对应的hostname
            final List<String> hostnameList = new ArrayList<>();
            final String hostname = getHostname(address);
            if (null != hostname && !hostname.equals(pcName)) {
                hostnameList.add(hostname);
            }
            hostnameList.add(pcName);

            final Set<String> workIdSet = snowflakeId.getWorkId();
            if (null == workIdSet || workIdSet.isEmpty()) {
                throw new IllegalStateException("None work-id configure");
            }

            final int workId = hostnameList.stream().map(host -> {
                // 在workIdSet中查找匹配hostname的配置
                final String findWorkId = workIdSet.stream().filter(o -> o.contains(host)).findFirst().get();
                if (null != findWorkId) {
                    final int offset = findWorkId.indexOf(Constants.COLON);
                    if (-1 == offset) {
                        // work-id 在yaml配置中没有":"，说明配置的格式不正确，直接返回MAX_VALUE
                        return Integer.MAX_VALUE;
                    }
                    final String strWorkId = findWorkId.substring(offset + Constants.COLON.length());
                    return Integer.valueOf(strWorkId);
                }
                return -1;
            }).findFirst().orElse(-1);

            if (-1 == workId) {  // 没有匹配此hostname的workId配置
                String errorMsg = new MessageFormat("no work-id configuration found for {0}: {1}")
                        .format(new Object[]{ (hostnameList.size() > 1 ? "hostnames" : "hostname"), hostnameList.stream().collect(Collectors.joining(" , ")) });
                throw new IllegalStateException(errorMsg);
            } else if (Integer.MAX_VALUE == workId) {  // workId配置格式不正确
                throw new IllegalStateException("work-id configuration format is incorrect, must be [hostname]:[work_id]");
            }

            // 检查workId是否有效
            rangeIn(workId, 0, (int) (com.sun.common.id.SnowflakeId.WORKER_ID_MAX_VALUE));

            // 若配置了雪花Id，则将workId设置到com.sun.common.id.SnowflakeId，对雪花Id生成器的WorkId初始化
            com.sun.common.id.SnowflakeId.setWorkId(workId);

        } catch (UnknownHostException e) {
            e.printStackTrace();
            throw new IllegalStateException("Unknown host, please set a proper hostname");
        }
    }



    // 根据ip address，在hosts文件中找到自定义的配置好的hostname
    private static String getHostname(final String address) {
        File file = new File(getHostsPath());
        if (file.isFile()) {
            try (final BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file)));) {
                String line;
                while (null != (line = reader.readLine())) {
                    final int offset = line.indexOf(address);
                    if (offset >= 0) {
                        String hostname = line.substring(offset + address.length());
                        hostname = StringUtil.strip(hostname);
                        // 排除名有"localhost"的hostname
                        if (!hostname.toLowerCase(Locale.ENGLISH).contains(Constants.LOCALHOST)) {
                            return hostname;
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
                throw new IllegalStateException(e.getCause());
            }
        }
        return null;
    }


    // 找hosts文件路径
    private static String getHostsPath() {
        // “Windows”, “Mac”, “Unix” and “Solaris”
        final String os = System.getProperty("os.name").toLowerCase(Locale.ENGLISH);
        return os.contains(Constants.OS_WIN_PREFIX) ? HOSTS_WINDOWS : HOSTS_DEFAULT;
    }


    // validate range in: [min, max]
    private static void rangeIn(final int current, int min, int max) {
        if (Math.max(0, current) != Math.min(current, max)) {
            throw new IllegalArgumentException(String.format("The given number %d can't be greater than %d or less than %d", current, max, min));
        }
    }
}
