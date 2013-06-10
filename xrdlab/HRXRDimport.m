function scan = HRXRDimport(filename)

	fd = fopen(filename, 'rt');

	scan.FirstAngle = NaN;
	scan.ScanRange = NaN;
	scan.StepWidth = NaN;
	scan.NrOfData = NaN;
	ScanAxisOK = 0;

	tline = fgetl(fd);

	% CR LF -> LF
	if length(tline) >= 1 && tline(end) == char(13)
		tline = tline(1:end-1);
	end

	assert(strcmp(tline,'HR-XRDScan'));

	while not(feof(fd))
	  tline = fgetl(fd); % Get line

		% CR LF -> LF
		if length(tline) >= 1 && tline(end) == char(13)
			tline = tline(1:end-1);
		end

		if length(tline)>12 && strcmp(tline(1:12), 'FirstAngle, ')
		  scan.FirstAngle = sscanf(tline, 'FirstAngle, %g', 1);
		end
		if length(tline)>13 && strcmp(tline(1:13), 'TimePerStep, ')
		  scan.TimePerStep = sscanf(tline, 'TimePerStep, %g', 1);
          scan.PhotonLevel = 1/scan.TimePerStep;
		end
		if length(tline)>11 && strcmp(tline(1:11), 'ScanRange, ')
		  scan.ScanRange = sscanf(tline, 'ScanRange, %g', 1);
		end
		if length(tline)>11 && strcmp(tline(1:11), 'StepWidth, ')
		  scan.StepWidth = sscanf(tline, 'StepWidth, %g', 1);
		end
		if length(tline)>10 && strcmp(tline(1:10), 'NrOfData, ')
		  scan.NrOfData = sscanf(tline, 'NrOfData, %g', 1);
		end
		if strcmp(tline, 'ScanAxis, Omega/2Theta')
			ScanAxisOK = 1;
		end
		if length(tline)>12 && strcmp(tline(1:12), 'Wavelength, ')
		  scan.lambda = sscanf(tline, 'Wavelength, %g', 1)*1e-10;
		end
		if strcmp(tline, 'ScanData')
			assert(ScanAxisOK);
			assert(not(isnan(scan.FirstAngle)));
			assert(not(isnan(scan.ScanRange)));
			assert(not(isnan(scan.StepWidth)));
			assert(not(isnan(scan.NrOfData)));
			assert(round(scan.ScanRange/scan.StepWidth + 1) == scan.NrOfData);
			scan.meas = fscanf(fd, '%g', scan.NrOfData);

			tline = fgetl(fd);
			% CR LF -> LF
			if length(tline) >= 1 && tline(end) == char(13)
				tline = tline(1:end-1);
			end
			assert(length(tline) == 0 || tline == (-1));
			assert(fgetl(fd) == -1);
			fclose(fd);
			scan.theta = linspace(scan.FirstAngle, scan.FirstAngle+scan.ScanRange, scan.NrOfData)' * pi/180;
			return;
		end
	end
	error('Invalid file format');
end
