package nl.tudelft.ewi.devhub.webtests.views;

import com.google.common.collect.Lists;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.List;

public class DiffInCommitView extends ProjectInCommitView {

	public DiffInCommitView(WebDriver driver) {
		super(driver);
	}

	/**
	 * @return the {@link DiffElement DiffElements} in this {@code DiffView}
	 */
	public List<DiffElement> listDiffs() {
		invariant();
		WebElement container = getDriver().findElement(By.className("container"));
		List<WebElement> elements = container.findElements(By.xpath("//div[@class='diff box']"));
		return Lists.transform(elements, DiffElement::build);
	}

	
}
