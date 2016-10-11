package com.blackducksoftware.sdk.protex.util.test;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.blackducksoftware.sdk.protex.common.ComponentColumn;
import com.blackducksoftware.sdk.protex.common.ComponentPageFilter;
import com.blackducksoftware.sdk.protex.util.PageFilterFactory;

public class PageFilterNextPageTest {
	
    @Test
    public void getNextPageZeroIndex() throws Exception {
    	//Create a page with start index 0 and end index 4
    	ComponentPageFilter pageFilter = PageFilterFactory.getFirstPage(5, ComponentColumn.NAME, true);
    	
    	//Next page should be 5-9
    	pageFilter = PageFilterFactory.getNextPage(pageFilter);
    	
    	Assert.assertEquals(pageFilter.getFirstRowIndex(), Integer.valueOf(5));
    	Assert.assertEquals(pageFilter.getLastRowIndex(), Integer.valueOf(9));
    	
    	//Next page should be 10-14
    	pageFilter = PageFilterFactory.getNextPage(pageFilter);
    	
    	Assert.assertEquals(pageFilter.getFirstRowIndex(), Integer.valueOf(10));
    	Assert.assertEquals(pageFilter.getLastRowIndex(), Integer.valueOf(14));
    	
    	//Next page should be 15-19
    	pageFilter = PageFilterFactory.getNextPage(pageFilter);
    	
    	Assert.assertEquals(pageFilter.getFirstRowIndex(), Integer.valueOf(15));
    	Assert.assertEquals(pageFilter.getLastRowIndex(), Integer.valueOf(19));
    }
    
    @Test
    public void getNextPageOneIndex() throws Exception {
    	//Create a page with start index 0 and end index 1
    	ComponentPageFilter pageFilter = PageFilterFactory.getFirstPage(2, ComponentColumn.NAME, true);
    	
    	//Set first page's start index to 1
    	pageFilter.setFirstRowIndex(1);
    	//Set first page's start index to 5
    	pageFilter.setLastRowIndex(5);
    	
    	//Next page should be 6-10
    	pageFilter = PageFilterFactory.getNextPage(pageFilter);
    	
    	Assert.assertEquals(pageFilter.getFirstRowIndex(), Integer.valueOf(6));
    	Assert.assertEquals(pageFilter.getLastRowIndex(), Integer.valueOf(10));
    	
    	//Next page should be 11-15
    	pageFilter = PageFilterFactory.getNextPage(pageFilter);
    	
    	Assert.assertEquals(pageFilter.getFirstRowIndex(), Integer.valueOf(11));
    	Assert.assertEquals(pageFilter.getLastRowIndex(), Integer.valueOf(15));
    	
    	//Next page should be 16-20
    	pageFilter = PageFilterFactory.getNextPage(pageFilter);
    	
    	Assert.assertEquals(pageFilter.getFirstRowIndex(), Integer.valueOf(16));
    	Assert.assertEquals(pageFilter.getLastRowIndex(), Integer.valueOf(20));
    }

}
