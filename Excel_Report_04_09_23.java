	
	=================== Controller START ====================
	@ResponseBody	
	@GetMapping("exportToExcelForViewBriefAllTransactions")
	public void exportToExcelForViewBriefAllTransactions(@RequestParam Map<String,String>params, 
			HttpSession session, HttpServletResponse httpServletResponse)
	{	
		System.out.println("#### exportToExcelForViewBriefAllTransactions :: ");
		
		String response = "";
		String userId = AppConstant.checkNull((String) session.getAttribute("username"));
		HttpStatus status = null;
		if (userId.length() > 0)
		{
			try {														
				response = dataAccessService.exportToExcelForViewBriefAllTransactions(params, userId, (String)session.getAttribute("role"), httpServletResponse);
				
					System.out.println("success"); 
				  
				 
				// xls file
					InputStream myStream = new ByteArrayInputStream(response.getBytes());
					httpServletResponse.addHeader("Content-disposition", "attachment;filename=ViewBriefAllTransactions.xls");
					httpServletResponse.setContentType("application/octet-stream");

				    // Copy the stream to the response's output stream.
				    IOUtils.copy(myStream, httpServletResponse.getOutputStream());
				    httpServletResponse.flushBuffer();				    
					 
				
				//return ResponseEntity.status(status).body(response);
			} catch (Exception e) {			
				e.printStackTrace();
			}
			
		}
		
	}
	
	
	=================== Controller END ====================
	
	
	================= Service START ======================
	
	@Override
	public String exportToExcelForViewBriefAllTransactions(Map<String, String> request, String userId,
			String role, HttpServletResponse response) {
		
		System.out.println("#### exportToExcelForViewBriefAllTransactions 123");
		// START
		StringBuilder sb = new StringBuilder();
		//boolean app_upload = false;		boolean pom_upload = false;		
		
		sb.append("<html><head><title>View Datewise Transaction Details</title></head><body bgcolor=\"#FFFFFF\">");

		ServerSideValidation ssv = new ServerSideValidation();

		String location = "", bankName = "", bankedIn = "", foliono = "", fundstatus = "", scheme_code = "";
		String viewby = AppConstant.checkNull(ssv.getRefindedString(request.get("viewby")));
		String firstTime = AppConstant.checkNull(ssv.getRefindedString(request.get("firsttime")));
		boolean isDataPresent = false;

		if (firstTime.equals("1")) {
			fundstatus = AppConstant.checkNull(ssv.getRefindedString(request.get("fundstatus")));
			scheme_code = AppConstant.checkNull(ssv.getRefindedString(request.get("scheme_code")));
			foliono = AppConstant.checkNull(ssv.getRefindedString(request.get("folioNo")));
			location = AppConstant.checkNull(ssv.getRefindedString(request.get("location")));
			bankName = AppConstant.checkNull(ssv.getRefindedString(request.get("bankName")));
			bankedIn = AppConstant.checkNull(ssv.getRefindedString(request.get("bankedIn")));
			System.out.println("#### bankedIn["+bankedIn+"]");
			if(bankedIn.length() > 0) {
				bankedIn = getBankedInBankCode(bankedIn);	
			}
			

		}
		System.out.println("#### fundstatus "+fundstatus+" scheme_code "+scheme_code+" "+foliono+" "+location);
		System.out.println("#### bankName "+bankName+" bankedIn "+bankedIn);
		
		String date_from = "";
		String date_to = "";
		String instrument_type = "", instrument_date = "", instrument_no = "";
		String drawn_on_bank = "", banked_in = "", receipt_status = "";
		String transaction_location = "";

		int receipt_status_count = 0;
		int datediff1 = 0;
		int counter = 0;
		int total_count = 0;

		java.util.Date date_today = new java.util.Date();

		sb.append("<br>");

		if (viewby.matches("datewise")) {
			date_from = AppConstant.checkNull(request.get("date_from"));
			date_to = AppConstant.checkNull(request.get("date_to"));

			sb.append("<table width=\"100%\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\"><tr></tr><tr></tr><tr>");
			sb.append(
					"<td colspan=\"16\"><font size=\"2\" face=\"Arial, Helvetica, sans-serif\"><strong>&nbsp;&#8226;");
			sb.append("DateWise Brief Detail of Transactions from "+date_from+" to"+date_to+"</strong></font></td>");
			sb.append("</tr><tr></tr></table>");

		} else {
			java.text.DateFormat df = new SimpleDateFormat("dd/MM/yyyy");
			date_from = df.format(date_today);
			date_to = df.format(date_today);

			sb.append("<table border=\"0\" cellpadding=\"0\" cellspacing=\"0\" width=\"100%\" align=\"center\">");
			sb.append("<tr></tr><tr></tr><tr valign=\"top\">");
			sb.append(
					"<td colspan=\"14\"><font size=\"2\" face=\"Arial, Helvetica, sans-serif\"><strong>&nbsp;&#8226;");
			sb.append("Brief Detail of Current Transactions for "+date_from+"</strong></font></td></tr>");

		}
		sb.append("<tr valign=\"top\"><td colspan=\"12\" align=\"center\" >&nbsp;</td></tr><tr></tr><tr></tr>");

		try {
			total_count = getDatewiseTransactionsCount(userId, role, date_from, date_to, location, bankedIn, foliono,fundstatus, scheme_code);
		} catch (Exception e) {			
			e.printStackTrace();
		}

		List<Object[]> rsList = viewAllDetailsOftransaction(userId, role, date_from, date_to, location, bankedIn,foliono, fundstatus, scheme_code);

		if (total_count > 0) {
			isDataPresent = true;
		}

		sb.append("<tr valign=\"top\"> <td colspan=\"12\" align=\"center\" >");

		String clr = "#ffffff";
		int count = 1;
		int srno = 0;
		//long amt;
		long sub_total = 0;
		boolean can_delete = false, lock_transaction = false;
		
		String transaction_id = "", investor_name = "", folio_no = "", transaction_type = "",curr_transaction_type = "";				
		String scheme_name = "", scheme_name_to = "", scheme_name_from = "" ;
		String fm_informed = "",   timestamp_no = "", remarks = "";
		String amt_formatted = "", transactionType = "";
		String created_by = "", aliasCode = "", accCode = "";
		
		//String doc_received_status = "", doc_forwarded_to = "", cams_confirmation_user_id = "", created_at = "",eisc_no = "";
		//String vouchered_by = "",application_ok = "", pom_ok = "", doc_sent_status,no_of_units = "", cams_code = "";//
		//String fund_utilised = "";
		
		
		
		if (isDataPresent) {

			sb.append("<table width=\"100%\" border=\"1\" align=\"center\" cellpadding=\"2\" cellspacing=\"0\" >");
			sb.append("<tr valign=\"top\">");
			sb.append(
					"<td width=\"4%\" align=\"center\" bgcolor=\"#004F9D\" > <font color=\"#FFFFFF\">Sr.No</font> </td>");
			sb.append(
					"<td width=\"4%\" align=\"center\" bgcolor=\"#004F9D\" > <font color=\"#FFFFFF\">Transaction Type</font> </td>");

			if (role.equals("OPERATIONS")) {
				sb.append("<td align=\"center\" bgcolor=\"#004F9D\" > <font color=\"#FFFFFF\"> Created By</font></td>");
			}

			sb.append(
					"<td width=\"18%\" align=\"center\" bgcolor=\"#004F9D\" > <font color=\"#FFFFFF\">Folio No</font></td>");
			sb.append(
					"<td width=\"18%\" align=\"center\" bgcolor=\"#004F9D\" > <font color=\"#FFFFFF\">Investor's Name</font></td>");
			sb.append(
					"<td width=\"6%\" align=\"center\" bgcolor=\"#004F9D\" > <p><font color=\"#FFFFFF\">Scheme  from </font></td>");
			sb.append(
					"<td width=\"6%\" align=\"center\" bgcolor=\"#004F9D\" ><font color=\"#FFFFFF\">Scheme to</font></td>");
			sb.append(
					"<td width=\"8%\" align=\"center\" bgcolor=\"#004F9D\" > <font color=\"#FFFFFF\">Amount (Rs) </font></td>");
			sb.append(
					"<td width=\"8%\" align=\"center\" bgcolor=\"#004F9D\" ><font color=\"#FFFFFF\">Instrument Type</font></td>");
			sb.append(
					"<td width=\"8%\" align=\"center\" bgcolor=\"#004F9D\" ><font color=\"#FFFFFF\">Instrument Date</font></td>");
			sb.append(
					"<td width=\"8%\" align=\"center\" bgcolor=\"#004F9D\" ><font color=\"#FFFFFF\">Instrument No</font></td>");
			sb.append(
					"<td width=\"8%\" align=\"center\" bgcolor=\"#004F9D\" ><font color=\"#FFFFFF\">Drawn On </font></td>");
			sb.append(
					"<td width=\"8%\" align=\"center\" bgcolor=\"#004F9D\" ><font color=\"#FFFFFF\">Banked In </font></td>");
			sb.append(
					"<td width=\"8%\" align=\"center\" bgcolor=\"#004F9D\" ><font color=\"#FFFFFF\">Credit Receipt Status</font></td>");
			sb.append(
					"<td width=\"8%\" align=\"center\" bgcolor=\"#004F9D\" > <font color=\"#FFFFFF\">Location </font></td>");

			if (!role.equals("SALES")) {
				sb.append(
						"<td width=\"8%\" align=\"center\" bgcolor=\"#004F9D\" ><font color=\"#FFFFFF\">Cycle Complete</font></td>");
			}
			sb.append(
					"<td width=\"8%\" align=\"center\" bgcolor=\"#004F9D\" ><font color=\"#FFFFFF\">Time stamp No</font></td>");
			sb.append(
					"<td width=\"8%\" align=\"center\" bgcolor=\"#004F9D\" ><font color=\"#FFFFFF\">Remarks</font></td>");
			sb.append(
					"<td width=\"8%\" align=\"center\" bgcolor=\"#004F9D\" ><font color=\"#FFFFFF\">Bank Account No</font></td>");
			sb.append(
					"<td width=\"8%\" align=\"center\" bgcolor=\"#004F9D\" ><font color=\"#FFFFFF\">Bank Code</font></td></tr>");

			for (Object[] data : rsList) {
				if (count % 2 == 0)
					clr = "#F0F8FF";
				else
					clr = "#ffffff";
				srno++;
								
				transactionType = AppConstant.chknull((String) data[3]);//rs.getString("TRANSACTION_TYPE");
				transaction_id = AppConstant.chknull((String) data[2]);//rs.getString("transaction_id");
				investor_name = AppConstant.chknull((String) data[5]);//rs.getString("investor_name");
				folio_no = AppConstant.checkNull((String) data[1]);//chknull(rs.getString("folio_no"));
				transaction_type = AppConstant.chknull((String) data[3]);//rs.getString("transaction_type");
				curr_transaction_type = AppConstant.chknull((String) data[3]);//rs.getString("transaction_type");
				scheme_name_to = AppConstant.chknull((String) data[24]);//rs.getString("scheme_to");
				scheme_name_from = AppConstant.chknull((String) data[23]);//rs.getString("scheme_from");
				remarks = AppConstant.chknull((String) data[18]);//rs.getString("remarks");
				created_by = AppConstant.chknull((String) data[4]);//rs.getString("user_id");
				timestamp_no = AppConstant.chknull((String) data[17]);//rs.getString("timestamp_no");				
				transaction_location = AppConstant.chknull((String) data[15]);//rs.getString("BRANCH_NAME");
				
				BigDecimal amt = (BigDecimal) data[8];//rs.getLong("amount_figure");				
				amt_formatted =  AppConstant.numFormatter(amt.longValue());
				sub_total = sub_total + amt.longValue();
				instrument_type = AppConstant.chknull((String) data[9]);//rs.getString("instrument_type");
				instrument_date = AppConstant.chknull((String) data[10]);//rs.getString("instrument_date");
				instrument_no = AppConstant.chknull((String) data[11]);//rs.getString("instrument_no");
				drawn_on_bank = AppConstant.chknull((String) data[12]);//rs.getString("drawn_on_bank");
				banked_in = AppConstant.chknull((String) data[13]);//rs.getString("deposite_bank_name");
				banked_in = AppConstant.chknull((String) data[21]);//rs.getString("bank_name");
				receipt_status = AppConstant.chknull((String) data[14]);//rs.getString("money_credited");
				fm_informed = AppConstant.checkNull((String) data[16]);//rs.getString("fund_mgr_informed_status");
				aliasCode = AppConstant.chknull((String) data[20]);//rs.getString("alias_code");
				accCode = AppConstant.chknull((String) data[19]);//rs.getString("acc_code");		

				fm_informed = AppConstant.chknull(fm_informed);

				if (fm_informed.equals("Yes")) {
					if (role.equals("SALES")) {
						lock_transaction = true;
					} else {
						lock_transaction = false;
					}
				} else {
					lock_transaction = false;
				}

				if (((AppConstant.chknull(scheme_name_to).equals("--")) && (transactionType.equals("Switch")))
						|| (AppConstant.chknull(drawn_on_bank).equals("--")) && transactionType.equals("Purchase")) {
					clr = "#FF0000";
				}
				
				sb.append("<tr class=\"ver1\">");
				sb.append("<td width=\"4%\" align = center ><span ><font color=\"#000000\">"+srno+"</font> </span> </td>");
				sb.append(
						"<td width=\"4%\" align = center ><span ><font color=\"#000000\">"+transactionType+"</font>  </span> </td>");

				if (role.equals("OPERATIONS")) {
					sb.append("<td align = center ><span ><font color=\"#000000\">"+created_by+"</font> </td>");
				}
				sb.append("<td width=\"15%\"  ><font color=\"#000000\">"+folio_no+"</font></td>");
				sb.append("<td width=\"15%\"  ><font color=\"#000000\">"+investor_name+"</font></td>");
				sb.append(
						"<td width=\"6%\" align = center  ><font color=\"#000000\">"+scheme_name_from+"</font></td>");
				if (!transactionType.equals("Switch")) {
					scheme_name_to = "--";
				}
				sb.append(
						"<td width=\"6%\" align = center  ><font color=\"#000000\">"+scheme_name_to+"</font></td>");
				sb.append("<td width=\"8%\" align = right  ><font color=\"#000000\">"+amt_formatted+"</font></td>");
				sb.append(
						"<td width=\"8%\" align = right  ><font color=\"#000000\">"+instrument_type+"</font></td>");
				sb.append(
						"<td width=\"8%\" align = center valign=\"middle\"  ><font color=\"#000000\">"+instrument_date+"</font></td>");
				sb.append(
						"<td width=\"8%\" align = right  ><font color=\"#000000\">"+instrument_no+"</font></td>");
				sb.append(
						"<td width=\"8%\" align = right  ><font color=\"#000000\">"+drawn_on_bank+"</font></td>");
				sb.append("<td width=\"8%\" align = right  ><font color=\"#000000\">"+banked_in+"</font></td>");
				sb.append("<td width=\"8%\" align = center valign=\"middle\">");

				if (transactionType.equals("Redemption") || transactionType.equals("Switch")) {
					sb.append("NA");
				} else {
					if (role.equals("OPERATIONS")) {
						if (receipt_status.equals("Before Cutoff") || receipt_status.equals("After Cutoff")) {
							sb.append("<font color=\"#000000\">"+receipt_status+"</font>");

						} else {
							sb.append("Not Received");
						}
					} else {
						sb.append("<font color=\"#000000\">"+receipt_status+"</font>");
					}
				}

				sb.append("</td><td width=\"8%\" align=\"center\"><font color=\"#000000\">" + transaction_location
						+ "</font></td>");
				if (!role.equals("SALES")) {
					can_delete = true;

					sb.append("<td width=\"8%\" align=\"center\"  ><font color=\"#000000\">" + fm_informed
							+ "</font></td>");
				} else {
					can_delete = true;
				}

				sb.append("<td width=\"8%\" align=\"center\"  ><font color=\"#000000\">"
						+ timestamp_no + " </font></td>");
				sb.append("<td width=\"8%\" align=\"center\"  ><font color=\"#000000\">" + remarks
						+ "</font></td>");
				sb.append("<td width=\"8%\" align=\"center\"  ><font color=\"#000000\">"+aliasCode+"</font></td>"); // T("=chknull(aliasCode)")
				sb.append("<td width=\"8%\" align=\"center\"  ><font color=\"#000000\">" + accCode
						+ "</font></td></tr>");
				count++;
			} // End of For

			if (role.equals("OPERATIONS")) {
				sb.append(
						"<tr valign=\"top\"> <td colspan=\"7\" align=\"right\" valign=\"middle\" class=\"ver1\">Sub Total(Rs.)</td>");
				sb.append("<td  align=\"right\" >"+sub_total+"</td><td colspan=\"12\" align=\"center\" >&nbsp;</td></tr>");
			} else {
				sb.append("<tr valign=\"top\">");
				sb.append("<td colspan=\"7\" align=\"right\" valign=\"middle\" class=\"ver1\">Sub Total (Rs.)</td>");
				sb.append("<td  align=\"right\" >"+sub_total+"</td><td colspan=\"12\" align=\"center\" >&nbsp;</td></tr>");
			}
			
			
			sb.append("<tr valign=\"top\"><td colspan=\"20\" align=\"center\" >&nbsp;</td></tr>");

			sub_total = 0;
			can_delete = false;
			
	}else{
			sb.append(
					"<tr><td colspan=\"20\" align = center> <strong> <font color=\"#FF0000\" size=\"2\" face=\"Arial, Helvetica, sans-serif\">");
			sb.append("<em>No Transaction Details Available </em></font></strong></td>");
		}
		
		sb.append("</tr></table></td></tr></table></body></html>");
		// END
		//System.out.println("#### "+sb.toString());
		return sb.toString();
	}
	
	==================================== Service End =======================================
	
	