#!/bin/sh
rm *.json
mongoexport --db lecarton --collection pastes --out lecarton_pastes_bak.json