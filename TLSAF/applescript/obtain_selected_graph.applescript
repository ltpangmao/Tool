
---scan graph information
tell application id "OGfl"
	tell front window
		set selectedShapes to selection
		set element_list to {}
		repeat with currentShape in selectedShapes
			set element_info to id of currentShape
			
			set element_list to element_list & element_info
			
		end repeat
		return element_list
	end tell
end tell
