/*
 * Copyright 2016 Google, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import com.google.classyshark.Shark;
import java.io.File;

public class Main {
	public static void main(String[] args) {
		File apk =
				new File("/Users/bfarber/Desktop/Scenarios/4 APKs/"
						+ "com.google.samples.apps.iosched-333.apk");
		Shark shark = Shark.with(apk);


		System.out.println(
				shark.getGeneratedClass("com.bumptech.glide.request.target.BaseTarget"));
		System.out.println(shark.getAllClassNames());
		System.out.println(shark.getManifest());
		System.out.println(shark.getAllMethods());

        System.out.println("\n\n\n\nAll Classes " + shark.getAllClassNames().size() +
        "\nAll Methods " + shark.getAllMethods().size());
	}
}