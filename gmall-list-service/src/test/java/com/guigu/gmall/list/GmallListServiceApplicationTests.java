package com.guigu.gmall.list;

import io.searchbox.client.JestClient;
import io.searchbox.core.Search;
import io.searchbox.core.SearchResult;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest
public class GmallListServiceApplicationTests {

	@Autowired
	private JestClient jestClient;



	@Test
	public void contextLoads() {
	}

	@Test
	public void testEs() throws IOException {
		/*
		1.定义 dsl语句
		2.定义要执行的动作
		3.jestClient执行动作
		4.获取返回结果
		 */
		String query="{\n" +
				"  \"query\": {\n" +
				"    \"term\": {\n" +
				"      \"actorList.name\": \"张译\"\n" +
				"    }\n" +
				"  }\n" +
				"}";
		Search search = new Search.Builder(query).addIndex("movie_chn").addType("movie").build();

		SearchResult result = jestClient.execute(search);

		List<SearchResult.Hit<HashMap, Void>> hits = result.getHits(HashMap.class);

		for (SearchResult.Hit<HashMap, Void> hit : hits) {
			HashMap source = hit.source;
			String name = (String)source.get("name");
			System.err.println(name);
		}
	}
}
