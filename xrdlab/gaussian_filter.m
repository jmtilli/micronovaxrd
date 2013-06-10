function filter = gaussian_filter(dalpha0rad, stddevrad, stddevs)
    if(stddevrad > 0)
		filterside = round(stddevs*stddevrad/dalpha0rad);
		filter = exp(-((dalpha0rad*((-filterside):filterside)/stddevrad).^2)/2);
		filter = filter / sum(filter);
	else
		filter = 1;
	end
end
